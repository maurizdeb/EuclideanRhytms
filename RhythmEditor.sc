RhythmEditor{


	*humanize { | sequence, clockRes, amt |

		var sequenceConverted = Array.fill(sequence.size(), {0});
	    var step_displacement, displaced_index, step16_index, nSteps, timeNow;

		nSteps = sequence.size()/(clockRes/4); //number of steps in 16th (equal to numHits parameter of the euclidean sequence)

		for(0, (nSteps-1), { arg seqStep;

			step16_index = seqStep*(clockRes/4);                 //position of each 16th step, according to the augmented clock resolution

			step_displacement = round(rrand(-32,32)*amt);        //displacement to be applied to humanize the straight sequence
			displaced_index = step16_index + step_displacement;  //new index at which we write the displaced (humanized) onset

			if( displaced_index >= 0,
				{
					sequenceConverted = sequenceConverted.put( displaced_index, sequence[step16_index]);

				},
				{
					sequenceConverted = sequenceConverted.put( ((nSteps*clockRes/4) + step_displacement), sequence[step16_index]);
			});

		});
		"activated".postln;

		//scheduling itself 4 bars later, to keep humanzing
		timeNow = TempoClock.default.beats;
		TempoClock.default.schedAbs(timeNow + 1024, { RhythmEditor.humanize(sequence, clockRes, amt) });

		^sequenceConverted;

	}

}
