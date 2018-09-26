StepStatesColors {

	const <active = "30ECF8",
	      <inactive = "FFFFFF",
	      <current = "80EB43",
	      <switchedOff = "4F5859";
}


StepArray {

	var <>stepArray, step;

	draw { | instView |

		stepArray = Array.new(16);

		for(0, 15, { arg i;
			step = StaticText(instView, Rect(i*12 + 10, 20, 10, 10));
			step.background = Color.fromHexString(StepStatesColors.inactive);
			stepArray.add(step);
		});

	}

	getElement { | index |
		^stepArray[index];
	}

	setState { | state, index |

		state.postln;

		if( state == "current",
			{stepArray[index].background = Color.green},

			{
				if(state == "active",
				{stepArray[index].background = Color.blue}, {})
			}
		);

	}
	
	run {
		Pdef(\sequenceView,
			Pbind(
				\type, \set,
				
			)
		);
	}
}
