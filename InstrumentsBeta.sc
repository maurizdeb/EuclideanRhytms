//TRY OUT THIS METHODS AS CONSTRUCTORS
	/**new{ arg container;
		^super.init();
		this.createView(container);
	}*/

/*	*initClass {
        // initialize OtherClass before continuing

    }*/

TemplateInstGUI{

	var m, instView;

	create{ | instGrid, instView_width, w_width, w_height, prop_height |

		//CREATES THE CONTAINER
		instView = CompositeView.new(instGrid, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		//CREATES THE MENU
		m = PopUpMenu(instView, Rect(0, 0, 180, 20));
		m.items = InstFactory.getInstuments;

		m.background_(Color.fromHexString("30ECF8"));  // only changes the look of displayed item
		m.stringColor_(Color.black);   // only changes the look of displayed item
		m.font_(Font("Courier", 13));   // only changes the look of displayed item

		m.action = { arg menu;
			var inst = InstFactory.getIstance(menu.item);
			inst.createView(instView, instView_width, w_width, w_height, prop_height);
			//[menu.value, menu.item].postln;
		};
	}
}

InstFactory {

	classvar <>instrumentId=500;

	*getIstance{ | name |

		var instance = case

		{ name == "kick" }   { instance = KickInst.new}

		{ name == "kick808" } { instance = nil }

		{ name == "snare" } { instance = SnareInst.new }

		{ name == "hi-hat" } { instance = HiHatInst.new }

		{ name == "sampler" }   { instance = SamplerInst.new }

		{ name == "cowbell" }   { instance = CowbellInst.new };

		instance.pdefId = this.prGenIstrumetId;
		^instance;

	}

	*getInstuments {
		^["kick", "kick808", "snare", "hi-hat", "sampler", "cowbell"];
	}

	//PRIVATE METHOD SHOULD NOT BE CALLED FROM OUTSIDE
	*prGenIstrumetId{
		this.instrumentId = this.instrumentId + 1;
		^this.instrumentId;
	}
}

Inst {

	classvar dim_knob_euclidean, dim_knob_sound;

	var <>sequence, <>soundSource, <>pdefId,
	    <>instLabel, muteBtn, closeBtn,
	    seqHitsKnob, seqLengthKnob, seqOffsetKnob, g;

	/*initializeSequencerGui INITIATES THE GUI RELATIVE TO A GENERIC INSTRUMENT*/
	initializeSequencerGui { | instView, w_width, w_height, prop_height |

		//SETTING COMMON GUI DIMENSIONS
		dim_knob_euclidean = (0.084*w_height).round;
		dim_knob_sound = (prop_height*0.105*w_height).round;

		//SETTING INSTRUMENT LABEL
		instLabel = StaticText(instView, Rect((0.02*w_width).round, (0.26*w_height).round, (0.21*w_width).round, (0.03*w_height).round));
		instLabel.background = Color.yellow;
		instLabel.align_(\center);

		/*-----Euclidean Rhythm Controls--------*/
		g = ControlSpec.new(1, 16, \lin, 1);
		seqLengthKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.02*w_height).round, dim_knob_euclidean, dim_knob_euclidean),"length",g,initVal:16);

		seqLengthKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqHitsKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.105*w_height).round,dim_knob_euclidean, dim_knob_euclidean),"hits",g,initVal:0);

		seqHitsKnob.action_({
			sequence = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqOffsetKnob = EZKnob(instView,Rect((0.26*w_width).round,(0.19*w_height).round, dim_knob_euclidean, dim_knob_euclidean),"offset",g,initVal:0);

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
		muteBtn.action_({arg butt;
			if(butt.value == 1, {soundSource.set(\mute, 0)},{soundSource.set(\mute, 1)});
		});

		/*---------CLOSE BUTTON-----------------*/
		closeBtn = Button(instView, Rect(0, 0, (0.009*w_height).round, (0.009*w_height).round));
		closeBtn.states_([
			["X", Color.white, Color.red];
		]);
		closeBtn.action_({
			instView.remove;
			soundSource.remove;
		});
	}

	/*UPDATES THE SEQUENCE, AFTER A CHANGE IN THE EUCLIDEAN RHYTHM PARAMETERS
	TEMPLATE METHOD PATTERN: REAL IMPLEMENTATION IN SUBCLASSES*/
	updateSequence{ | sequence |
		^nil
	}

}


KickInst : Inst {

	var instView, kickTextBtn, decayKnob, punchKnob, compKnob,
	    g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | containter, instView_width, w_width , w_height, prop_height|

		instView = CompositeView.new(containter, Rect(0, 0, (instView_width/2) - 7, (w_width/3) - 7 ) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView, w_width, w_height, prop_height);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "kick";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \kick,
				\noteOrRest, Pif((Pseq([0,0,0,0], inf))> 0, 1, Rest)
			)
		).play(quant:4);

		/* ---- Kick_Decay_Knob -----*/
		g = ControlSpec.new(0.1, 8, \lin);
			decayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,dim_knob_sound, dim_knob_sound),"decay",g,initVal:1);

		decayKnob.action_({
			super.soundSource.set(\decay,decayKnob.value);
		});
		/* ---- Kick_Punch_Knob -----*/
		g = ControlSpec.new(0, 1, \lin);
			punchKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"punch",g,initVal:0.55);

		punchKnob.action_({
			super.soundSource.set(\punch,punchKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
	updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \kick,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
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

		super.instLabel.string = "snare";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \snare,
				//\level, Pseq([0,0,0,0], inf)
				\noteOrRest, Pif((Pseq([0,0,0,0], inf))> 0, 1, Rest)
			)
		).play(quant:4);

		/* ---- Snare_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		snareDecayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round,dim_knob_sound, dim_knob_sound),"decay",g,initVal:0.1);

		snareDecayKnob.action_({
			super.soundSource.set(\decay,snareDecayKnob.value);
		});

		/* ---- Snare_Tone_Knob -----*/
		g = ControlSpec.new(0.7, 1.3, \lin);
		snareToneKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

		snareToneKnob.action_({
			super.soundSource.set(\tone,snareToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{| sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \snare,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
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

		super.instLabel.string = "hi-hat";

		super.soundSource = Pdef(super.pdefId, //va fatta partire una sola volta questa merda di pdef
			Pbind(
				\instrument, \hi_hat,
				\noteOrRest, Pif((Pseq([0,0,0,0], inf))>0,1,Rest)
			)
		).play(quant:4);

		/* ---- Hi-hat_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		hi_hatDecayKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"decay",g,initVal:0.1);

		hi_hatDecayKnob.action_({
			super.soundSource.set(\opening,hi_hatDecayKnob.value);
		});

		/* ---- Hi-hat_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		hi_hatToneKnob = EZKnob(instView,Rect((0.13*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

		hi_hatToneKnob.action_({
			super.soundSource.set(\freq,hi_hatToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \hi_hat,
				\noteOrRest, Pif(seq>0,1,Rest())
			)
		);
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

		super.instLabel.string = "sampler";

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
					super.soundSource = Pdef(super.pdefId,
						Pbind(
							\instrument, \sampler,
							\noteOrRest, Pif((Pseq([0,0,0,0], inf))>0,1,Rest)
						)
					).play(quant:4);
					//super.soundSource.set(\t_gate, 0);
				},
				{},
				fileMode: 1, acceptMode: 0,
				stripResult: false);
		});

		/* ---- sample rate knob -----*/
		g = ControlSpec.new(0, 1, \lin);
		sample_rate_knob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"rate", g,initVal:0.5);

		sample_rate_knob.action_({
			super.soundSource.set(\rate, sample_rate_knob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		super.soundSource = Pdef(
			super.pdefId,
			Pbind(
				\instrument, \sampler,
				\noteOrRest, Pif(seq>0,1,Rest())
		));
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

		super.instLabel.string = "cowbell";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \cowbell,
				\noteOrRest, Pif((Pseq([0,0,0,0], inf))>0, 1, Rest)
			)
		).play(quant:4);

		/* ---- cowbell_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		cowbellToneKnob = EZKnob(instView,Rect(((10/1024)*w_width).round,(0.01*w_height).round, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

		cowbellToneKnob.action_({
			super.soundSource.set(\fund_freq,240+(cowbellToneKnob.value*700));
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \cowbell,
				\noteOrRest, Pif(seq>0, 1, Rest())
			)
		);
		}
}
