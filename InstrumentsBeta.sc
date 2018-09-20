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
	initializeSequencerGui { | instView, w_width, w_height, prop_height |

		/*-----Euclidean Rhythm Controls--------*/
		g = ControlSpec.new(1, 16, \lin, 1);
		seqLengthKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.02*w_height).round,(0.084*w_height).round,(0.084*w_height).round),"length",g,initVal:16);

		seqLengthKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqHitsKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.105*w_height).round,(0.084*w_height).round,(0.084*w_height).round),"hits",g,initVal:0);

		seqHitsKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqOffsetKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.19*w_height).round,(0.084*w_height).round,(0.084*w_height).round),"offset",g,initVal:0);

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
	createView{ | containter, instView_width, w_width , w_height, prop_height|

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		kickPlayBtn = Button(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		kickPlayBtn.string = "kik play";

		kickPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(2,8);
			~kickSeq.play(quant:4);

		});
		/* ---- Kick_Decay_Knob -----*/
		g = ControlSpec.new(0, 3, \lin);
			decayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"decay",g,initVal:0.5);

		decayKnob.action_({
			super.soundSource.set(\decay,decayKnob.value);
		});
		/* ---- Kick_Punch_Knob -----*/
		g = ControlSpec.new(0, 1, \lin);
			punchKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"punch",g,initVal:0.55);

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

	var instView, snarePlayBtn, snareDecayKnob, snareToneKnob, snareSeq, g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		snarePlayBtn = Button(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		snarePlayBtn.string = "snr play";

		snarePlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(3,7);
			snareSeq.play(quant:4);
		});

		/* ---- Snare_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		snareDecayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"decay",g,initVal:0.1);

		snareDecayKnob.action_({
			super.soundSource.set(\decay,snareDecayKnob.value);
		});

		/* ---- Snare_Tone_Knob -----*/
		g = ControlSpec.new(0.7, 1.3, \lin);
		snareToneKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"tone",g,initVal:1);

		snareToneKnob.action_({
			super.soundSource.set(\tone,snareToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
			snareSeq = Pdef(
			"snareSeq" ++ super.soundSource.asNodeID.asString,
				Pbind(
					\type, \set,
					\id, super.soundSource.asNodeID,
					//\instument, \snare,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
		)); //IL PROBLEMA E' QUI MA NON CAPISCO LA RAGIONE
		}

}

HiHatInst : Inst {

	var instView, hi_hatPlayBtn, hi_hatDecayKnob, hi_hatToneKnob,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		hi_hatPlayBtn = Button(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		hi_hatPlayBtn.string = "hi-hat play";

		hi_hatPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(5,13);
			~hi_hatSeq.play(quant:4);
		});

		/* ---- Hi-hat_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		hi_hatDecayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"decay",g,initVal:0.1);

		hi_hatDecayKnob.action_({
			super.soundSource.set(\opening,hi_hatDecayKnob.value);
		});

		/* ---- Hi-hat_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		hi_hatToneKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"tone",g,initVal:1);

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
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		samplerPlayBtn = Button(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		samplerPlayBtn.string = "sample play";

		samplerPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(4,15);
			~sapler1Seq.play(quant:4);
		});

		/* ---- btnLoad -----*/
		samplerLoadBtn = Button(instView,Rect((0.02*w_width).round, (0.18*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
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
		sample_rate_knob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"rate", g,initVal:0.5);

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

CowbellInst : Inst {

	var instView, cowbellPlayBtn, cowbellToneKnob, g;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnPlay -----*/
		cowbellPlayBtn = Button(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		cowbellPlayBtn.string = "cowbell play";

		cowbellPlayBtn.action_({ arg butt;
			butt.value.asBoolean.not.postln;
			super.sequence = EuclideanRhythmGen.compute_rhythm(5,13);
			~cowbellSeq.play(quant:4);
		});

		/* ---- cowbell_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		cowbellToneKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,(prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"tone",g,initVal:1);

		cowbellToneKnob.action_({
			super.soundSource.set(\fund_freq,240+(cowbellToneKnob.value*700));
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
			~cowbellSeq = Pdef(
				\cowbellSeq,
				Pbind(
					\type, \set,
					\id, super.soundSource,
					\instument, \hi_hat,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
			));
		}
}
