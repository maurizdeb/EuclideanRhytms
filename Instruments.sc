//TRY OUT THIS METHODS AS CONSTRUCTORS
	/**new{ arg container;
		^super.init();
		this.createView(container);
	}*/

/*	*initClass {
        // initialize OtherClass before continuing

    }*/

//Class variables are values that are shared by all objects in the class
	//USE THIS VARS FOR GENERAL GUI LAYOUT e.g.
	//classvar seq_length_knob_pos

Inst {

	var <>sequence, <>soundSource, seqHitsKnob, seqLengthKnob, seqOffsetKnob, g;

	/*initializeSequencerGui INITIATES THE GUI RELATIVE TO A GENERIC INSTRUMENT*/
	initializeSequencerGui { | instView |

		/*-----Euclidean Rhythm Controls--------*/
		g = ControlSpec.new(1, 16, \lin, 1);
		seqLengthKnob = EZKnob(instView,Rect(265,20,80,80),"length",g,initVal:16);

		seqLengthKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqHitsKnob = EZKnob(instView,Rect(265,100,80,80),"hits",g,initVal:0);

		seqHitsKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqOffsetKnob = EZKnob(instView,Rect(265,180,80,80),"offset",g,initVal:0);

		seqOffsetKnob.action_({
			if(seqOffsetKnob.value < seqLengthKnob.value, {
				var zeroOffsetSeq;
				zeroOffsetSeq = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
				sequence = zeroOffsetSeq.rotate(seqOffsetKnob.value.asInteger);
				this.updateSequence(sequence);
			});

		});
	}

	/*UPDATES THE SEQUENCE, AFTER A CHANGE IN THE EUCLIDEAN RHYTHM PARAMETERS
	TEMPLATE METHOD PATTERN: REAL IMPLEMENTATION IN SUBCLASSES*/
	updateSequence{ | sequence |
		^nil
	}

}


KickInst : Inst {

	var instView, kickPlayBtn, decayKnob, punchKnob,
	    g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		kickPlayBtn = Button(instView, Rect(20, 250, 210, 30));
		kickPlayBtn.string = "kik play";

		kickPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(2,8);
			~kickSeq.play(quant:4);

		});
		/* ---- Kick_Decay_Knob -----*/
		g = ControlSpec.new(0, 3, \lin);
		decayKnob = EZKnob(instView,Rect(10,10,100,100),"decay",g,initVal:0.5);

		decayKnob.action_({
			super.soundSource.set(\decay,decayKnob.value);
		});
		/* ---- Kick_Punch_Knob -----*/
		g = ControlSpec.new(0, 1, \lin);
		punchKnob = EZKnob(instView,Rect(130,10,100,100),"punch",g,initVal:0.55);

		punchKnob.action_({
			super.soundSource.set(\punch,punchKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
	updateSequence{ | sequence |
		~kickSeq = Pdef(
			\kickSeq,
			Pbind(
				\type, \set,
				\id, super.soundSource,
				\instument, \kick,
				\args, #[\t_gate],
				\t_gate, Pseq(sequence, inf),
		));
	}
}

SnareInst : Inst {

	var instView, snarePlayBtn, snareDecayKnob, snareToneKnob,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		snarePlayBtn = Button(instView, Rect(20, 250, 210, 30));
		snarePlayBtn.string = "snr play";

		snarePlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(3,7);
			~snareSeq.play(quant:4);
		});

		/* ---- Snare_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		snareDecayKnob = EZKnob(instView,Rect(10,10,100,100),"decay",g,initVal:0.1);

		snareDecayKnob.action_({
			super.soundSource.set(\decay,snareDecayKnob.value);
		});

		/* ---- Snare_Tone_Knob -----*/
		g = ControlSpec.new(0.7, 1.3, \lin);
		snareToneKnob = EZKnob(instView,Rect(130,10,100,100),"tone",g,initVal:1);

		snareToneKnob.action_({
			super.soundSource.set(\tone,snareToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
			~snareSeq = Pdef(
				\snareSeq,
				Pbind(
					\type, \set,
					\id, super.soundSource,
					\instument, \snare,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
			));
		}

}

HiHatInst : Inst {

	var instView, hi_hatPlayBtn, hi_hatDecayKnob, hi_hatToneKnob,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		hi_hatPlayBtn = Button(instView, Rect(20, 250, 210, 30));
		hi_hatPlayBtn.string = "hi-hat play";

		hi_hatPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(5,13);
			~hi_hatSeq.play(quant:4);
		});

		/* ---- Hi-hat_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		hi_hatDecayKnob = EZKnob(instView,Rect(10,10,100,100),"decay",g,initVal:0.1);

		hi_hatDecayKnob.action_({
			super.soundSource.set(\opening,hi_hatDecayKnob.value);
		});

		/* ---- Hi-hat_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		hi_hatToneKnob = EZKnob(instView,Rect(130,10,100,100),"tone",g,initVal:1);

		hi_hatToneKnob.action_({
			super.soundSource.set(\freq,hi_hatToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
			~hi_hatSeq = Pdef(
				\hi_hatSeq,
				Pbind(
					\type, \set,
					\id, super.soundSource,
					\instument, \hi_hat,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
			));
		}

}

SamplerInst : Inst {

	var instView, samplerPlayBtn, samplerLoadBtn, sample_rate_knob, b,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		samplerPlayBtn = Button(instView, Rect(20, 250, 210, 30));
		samplerPlayBtn.string = "sample play";

		samplerPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(4,15);
			~sapler1Seq.play(quant:4);
		});

		/* ---- btnLoad -----*/
		samplerLoadBtn = Button(instView,Rect(20, 175, 210, 30));
		samplerLoadBtn.string = "load sample";

		samplerLoadBtn.action_({ arg butt;
			FileDialog.new(
				{//FUNCTION ON FILE SELECTION
					arg file;

					var path = file[0];
					// read a soundfile
					b = Buffer.read(Server.local, path);

					b.bufnum.postln;

					super.soundSource.set(\buf, b.bufnum);
					super.soundSource.set(\t_gate, 1);
				},
				{},
				fileMode: 1, acceptMode: 0,
				stripResult: false);
		});

		/* ---- sample rate knob -----*/
		g = ControlSpec.new(0, 1, \lin);
		sample_rate_knob = EZKnob(instView,Rect(10,10,100,100),"rate", g,initVal:0.5);

		sample_rate_knob.action_({
			super.soundSource.set(\rate,sample_rate_knob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
			~sapler1Seq = Pdef(
				\sapler1Seq,
				Pbind(
					\type, \set,
					\id, super.soundSource,
					\instument, \sampler,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
			));
		}

}
