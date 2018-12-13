//TRY OUT THIS METHODS AS CONSTRUCTORS
	/**new{ arg container;
		^super.init();
		this.createView(container);
	}*/

/*	*initClass {
        // initialize OtherClass before continuing

    }*/

TemplateInstGUI{

	var m, templateView;

	create{ | instGrid |

		var instGrid_width = instGrid.bounds.width;
		var templateView_spacing = 7;//spacing between the various templateViews

		//CREATES THE CONTAINER "templateView"
		templateView = CompositeView.new(instGrid, Rect(0, 0, (instGrid_width/2) - templateView_spacing, (instGrid_width*2/3) - templateView_spacing ) );
		templateView.background = Color.grey;

		//CREATES A MENU IN IT
		m = PopUpMenu(templateView, Rect(0, 0, instGrid_width/4, instGrid_width/20));
		m.items = InstFactory.getInstuments;

		m.background_(Color.fromHexString("30ECF8"));  // only changes the look of displayed item
		m.stringColor_(Color.black);   // only changes the look of displayed item
		m.font_(Font("AR DESTINE", 10));   // only changes the look of displayed item

		m.action = { arg menu;
			if(menu.item!="SELECT INSTRUMENT",
				{var inst = InstFactory.getIstance(menu.item);
					inst.createView(templateView);
			}, {});

			//[menu.value, menu.item].postln;
		};
	}
}

InstFactory {

	classvar <>instrumentId=500;

	*getIstance{ | name |

		var instance = case

		{ name == "kick" }   { instance = KickInst.new}

		//{ name == "kick808" } { instance = nil }

		{ name == "snare" } { instance = SnareInst.new }

		{ name == "hi-hat" } { instance = HiHatInst.new }

		{ name == "sampler" }   { instance = SamplerInst.new }

		{ name == "cowbell" }   { instance = CowbellInst.new };

		instance.pdefId = this.prGenIstrumetId;
		^instance;

	}

	*getInstuments {
		^["SELECT INSTRUMENT","kick", /*"kick808",*/ "snare", "hi-hat", "sampler", "cowbell"];
	}

	//PRIVATE METHOD SHOULD NOT BE CALLED FROM OUTSIDE
	*prGenIstrumetId{
		this.instrumentId = this.instrumentId + 1;
		^this.instrumentId;
	}
}

Inst {

	classvar dim_knob_euclidean, dim_knob_sound;

	var sequence16, <>sequence, <>sequenceStraight, <>soundSource, <>pdefId,
	    <>instLabel, muteBtn, <>closeBtn,
	    seqHitsKnob, seqLengthKnob, seqOffsetKnob, humanizerKnob, g;

	/*initializeSequencerGui INITIATES THE GUI RELATIVE TO A GENERIC INSTRUMENT*/
	initializeSequencerGui { | instView |

		var instView_width = instView.bounds.width;
		var instView_heigth = instView.bounds.height;

		//SETTING COMMON GUI DIMENSIONS
		dim_knob_euclidean = instView.bounds.width/5;
		dim_knob_sound = instView.bounds.width/4;

		//SETTING INSTRUMENT LABEL
		instLabel = StaticText(instView, Rect(instView_width/4, instView_heigth/30, instView_width/2, instView_heigth/20));
		instLabel.font_("AR DESTINE", 30);
		instLabel.background = Color.fromHexString("BADFFF");
		instLabel.align_(\center);

		/*-----Euclidean Rhythm Controls--------*/


		g = ControlSpec.new(1, 16, \lin, 1);
		seqLengthKnob = EZKnob(instView,Rect(instView_width/16, instView_width*3/4, dim_knob_euclidean, dim_knob_euclidean),"length",g,initVal:16);

		seqLengthKnob.action_({
			sequence16 = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value); //in 16th notes
			sequenceStraight = EuclideanRhythmGen.convertToClockResolution(sequence16, 256);
			sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
			soundSource.quant_([sequence.size(),0,0,1]);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqHitsKnob = EZKnob(instView,Rect(instView_width*5/16, instView_width*3/4, dim_knob_euclidean, dim_knob_euclidean),"hits",g,initVal:0);

		seqHitsKnob.action_({
			sequence16 = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value); //in 16th notes
			sequenceStraight = EuclideanRhythmGen.convertToClockResolution(sequence16, 256);
			sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqOffsetKnob = EZKnob(instView,Rect(instView_width*9/16, instView_width*3/4, dim_knob_euclidean, dim_knob_euclidean),"offset",g,initVal:0);

		seqOffsetKnob.action_({
			if(seqOffsetKnob.value < seqLengthKnob.value, {
				var zeroOffsetSeq;
				zeroOffsetSeq = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value);
				sequence16 = zeroOffsetSeq.rotate(seqOffsetKnob.value.asInteger); //in 16th notes
				sequenceStraight = EuclideanRhythmGen.convertToClockResolution(sequence16, 256);
				sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
				this.updateSequence(sequence);
			});

		});

		/*--------HUMANIZER CONTROL-----*/

		g = ControlSpec.new(0, 1, \lin, 0.1);
		humanizerKnob = EZKnob(instView,Rect(instView_width*13/16, instView_width*3/4, dim_knob_euclidean, dim_knob_euclidean),"humanizer",g,initVal:0);

		humanizerKnob.action_({
		});

		/*--------MUTE BUTTON-----------*/

		muteBtn = Button(instView, Rect(instView_width*7/15, instView_heigth/10, instView_width/15, instView_width/15));
		muteBtn.states_([
			["M", Color.black, Color.white],
			["M", Color.black, Color.red];
		]);
		muteBtn.action_({arg butt;
			this.muteStrategy(butt);
		});

		/*---------CLOSE BUTTON-----------------*/
		closeBtn = Button(instView, Rect(0, 0, instView_width/40, instView_width/40) );
		closeBtn.font("Bahnschrift");
		closeBtn.states_([
			["X", Color.white, Color.red];
		]);
		closeBtn.action_({
			instView.remove;
			this.removePdef;
		});

		sequence = Array.fill(1024, {0});
		sequenceStraight = Array.fill(1024, {0});
		//must be called once
		this.runHumanizer(sequence, 256);
	}

	runHumanizer{ | clockRes |

		var timeNow;

		sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
		this.updateSequence(sequence);

		//scheduling itself 4 bars later, to keep humanzing
		timeNow = TempoClock.default.beats;
		TempoClock.default.schedAbs(timeNow + sequence.size(), {this.runHumanizer(clockRes)});
	}

	/*UPDATES THE SEQUENCE, AFTER A CHANGE IN THE EUCLIDEAN RHYTHM PARAMETERS
	TEMPLATE METHOD PATTERN: REAL IMPLEMENTATION IN SUBCLASSES*/
	updateSequence{ | sequence |
		^nil
	}

	muteStrategy{ | muteBtn |
		if(muteBtn.value == 1, {soundSource.set(\mute, 0)},{soundSource.set(\mute, 1)});
	}

	removePdef{
		^nil
	}

}


KickInst : Inst {

	var instView, kickTextBtn, decayKnob, punchKnob, compKnob,
	    g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "KICK";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \kick,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))> 0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.play;

				/* ---- Kick_Decay_Knob -----*/
		g = ControlSpec.new(0.1, 8, \lin);
			decayKnob = EZKnob(instView, Rect(instView.bounds.left + 45, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"decay",g,initVal:1);

		decayKnob.action_({
			super.soundSource.set(\decay,decayKnob.value);
		});
		/* ---- Kick_Punch_Knob -----*/
		g = ControlSpec.new(0, 1, \lin);
			punchKnob = EZKnob(instView, Rect(instView.bounds.left + 240, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"punch",g,initVal:0.55); //TODO scalare le posizioni dei knobs punch e decay

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

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

SnareInst : Inst {

	var instView, snareTextBtn, snareDecayKnob, snareToneKnob, g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "snare";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \snare,
				//\level, Pseq([0,0,0,0], inf)
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))> 0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.play;

		/* ---- Snare_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		snareDecayKnob = EZKnob(instView, Rect(instView.bounds.left + 45, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"decay",g,initVal:0.1);

		snareDecayKnob.action_({
			super.soundSource.set(\decay,snareDecayKnob.value);
		});

		/* ---- Snare_Tone_Knob -----*/
		g = ControlSpec.new(0.7, 1.3, \lin);
		snareToneKnob = EZKnob(instView, Rect(instView.bounds.left + 240, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

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

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

HiHatInst : Inst {

	var instView, hi_hatTextBtn, hi_hatDecayKnob, hi_hatToneKnob,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "hi-hat";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \hi_hat,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0,1,Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.play;

		/* ---- Hi-hat_Decay_Knob -----*/
		g = ControlSpec.new(0.05, 1, \lin);
		hi_hatDecayKnob = EZKnob(instView, Rect(instView.bounds.left + 45, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"decay",g,initVal:0.1);

		hi_hatDecayKnob.action_({
			super.soundSource.set(\opening,hi_hatDecayKnob.value);
		});

		/* ---- Hi-hat_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		hi_hatToneKnob = EZKnob(instView, Rect(instView.bounds.left + 240, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

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
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}

}

SamplerInst : Inst {

	var instView, samplerTextBtn, samplerLoadBtn, sample_rate_knob, b, synth,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "sampler";

		/* ---- btnLoad -----*/
		samplerLoadBtn = Button(instView, Rect(instView.bounds.left + 40, instView.bounds.top + 100, templateView.bounds.width/5, templateView.bounds.width/20));
		samplerLoadBtn.string = "load sample";

		samplerLoadBtn.action_({ arg butt;
			FileDialog.new(
				{//FUNCTION ON FILE SELECTION
					arg file;

					var path = file[0];
					// read a soundfile
					b = Buffer.read(Server.local, path);

					b.bufnum.postln;

					synth = Synth(\sampler);
					synth.set(\buf, b.bufnum);
					super.soundSource = Pdef(super.pdefId,
						Pbind(
							\type, \set,
							\id, synth.asNodeID,
							//\instrument, \sampler,
							//\noteOrRest, Pif((Pseq([0,0,0,0], inf))>0,1,Rest)
							\args, #[\t_gate],
							\t_gate, Pseq(Array.fill(1024, {0}),inf)
						)
					);
					super.soundSource.quant_([1024,0,0,1]);
					super.soundSource.play;
					//super.soundSource.set(\t_gate, 0);
				},
				{},
				fileMode: 1, acceptMode: 0,
				stripResult: false);
		});

		/* ---- sample rate knob -----*/
		g = ControlSpec.new(0, 3, \lin);
		sample_rate_knob = EZKnob(instView, Rect(instView.bounds.left + 240, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"rate", g,initVal:0.5);

		sample_rate_knob.action_({
			synth.set(\rate, sample_rate_knob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\type, \set,
				\id, synth.asNodeID,
				//\instrument, \sampler,
				//\noteOrRest, Pif((Pseq([0,0,0,0], inf))>0,1,Rest)
				\args, #[\t_gate],
				\t_gate, seq
			)
		);
		}

	removePdef{
		synth.free;
		Pdef(super.pdefId).remove;
	}

	muteStrategy{ | muteBtn |
		if(muteBtn.value == 1, {synth.set(\mute, 0)},{synth.set(\mute, 1)});
	}

}

CowbellInst : Inst {

	var instView, cowbellTextBtn, cowbellToneKnob, g;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "cowbell";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \cowbell,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.play;

		/* ---- cowbell_Tone_Knob -----*/
		g = ControlSpec.new(0.83, 1.6, \lin);
		cowbellToneKnob = EZKnob(instView, Rect(instView.bounds.left + 45, instView.bounds.top + 100, dim_knob_sound, dim_knob_sound),"tone",g,initVal:1);

		cowbellToneKnob.action_({
			super.soundSource.set(\fund_freq,240+(cowbellToneKnob.value*300));
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \cowbell,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}
