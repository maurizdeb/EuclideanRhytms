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

	var <>sequence, <>soundSource, ampInst, seqHitsKnob, seqLengthKnob, seqOffsetKnob, g, muteBtn;

	/*initializeSequencerGui INITIATES THE GUI RELATIVE TO A GENERIC INSTRUMENT*/
	initializeSequencerGui { | instView, w_width, w_height, prop_height |

		soundSource.get(\amp, {arg value; ampInst = value});

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

		/*--------MUTE BUTTON-----------*/

		muteBtn = Button(instView, Rect((0.02*w_width).round, (0.21*w_height).round,(0.09*w_width).round, (0.03*w_height).round));
		muteBtn.states_([
			["mute", Color.black, Color.white],
			["mute", Color.black, Color.red];
		]);
		muteBtn.background = Color.white;
		muteBtn.action_({arg butt;
			if(butt.value == 1, {soundSource.set(\amp, 0.0)},{soundSource.set(\amp, ampInst)});
		});
	}

	/*UPDATES THE SEQUENCE, AFTER A CHANGE IN THE EUCLIDEAN RHYTHM PARAMETERS
	TEMPLATE METHOD PATTERN: REAL IMPLEMENTATION IN SUBCLASSES*/
	updateSequence{ | sequence |
		^nil
	}

}


KickInst : Inst {

	var instView, kickTextBtn, decayKnob, punchKnob,
	    g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width , w_height, prop_height|

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		kickTextBtn = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		kickTextBtn.string = "kick";
		kickTextBtn.background = Color.yellow;
		kickTextBtn.align_(\center);

		/*----- Mute Btn Kick --------*/

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
		Pdef(
			\kickSeq ++ super.soundSource.asNodeID,
			Pbind(
				\type, \set,
				\id, super.soundSource.asNodeID,
				//\instument, \kick,
				\args, #[\t_gate],
				\t_gate, Pseq(sequence, inf),
		)).play(quant:4);
	}
}

SnareInst : Inst {

	var instView, snareTextBtn, snareDecayKnob, snareToneKnob, g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnText -----*/
		snareTextBtn = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		snareTextBtn.string = "snare";
		snareTextBtn.background = Color.yellow;
		snareTextBtn.align_(\center);

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
			Pdef(
			\snareSeq ++ super.soundSource.asNodeID,
				Pbind(
					\type, \set,
					\id, super.soundSource.asNodeID,
					//\instument, \snare,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
		)).play(quant:4); //IL PROBLEMA E' QUI MA NON CAPISCO LA RAGIONE
		}

}

HiHatInst : Inst {

	var instView, hi_hatTextBtn, hi_hatDecayKnob, hi_hatToneKnob,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnText -----*/
		hi_hatTextBtn = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		hi_hatTextBtn.string = "hi-hat";
		hi_hatTextBtn.background = Color.yellow;
		hi_hatTextBtn.align = \center;

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
			Pdef(
			\hi_hatSeq ++ super.soundSource.asNodeID,
				Pbind(
					\type, \set,
					\id, super.soundSource.asNodeID,
					//\instument, \hi_hat,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
		)).play(quant:4);
		}

}

SamplerInst : Inst {

	var instView, samplerTextBtn, samplerLoadBtn, sample_rate_knob, b,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnText -----*/
		samplerTextBtn = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		samplerTextBtn.string = "sampler";
		samplerTextBtn.background = Color.yellow;
		samplerTextBtn.align_(\center);

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
					super.soundSource.set(\t_gate, 0);
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
			Pdef(
				\sapler1Seq ++ super.soundSource.asNodeID,
				Pbind(
					\type, \set,
					\id, super.soundSource.asNodeID,
					//\instument, \sampler,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
		)).play(quant:4);
		}

}

CowbellInst : Inst {

	var instView, cowbellTextBtn, cowbellToneKnob, g;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width, w_height, prop_height |

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		/* ---- btnText -----*/
		cowbellTextBtn = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		cowbellTextBtn.string = "cowbell";
		cowbellTextBtn.background = Color.yellow;
		cowbellTextBtn.align_(\center);

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
			Pdef(
				\cowbellSeq ++ super.soundSource.asNodeID,
				Pbind(
					\type, \set,
					\id, super.soundSource.asNodeID,
					//\instument, \hi_hat,
					\args, #[\t_gate],
					\t_gate, Pseq(sequence, inf),
		)).play(quant:4);
		}
}
