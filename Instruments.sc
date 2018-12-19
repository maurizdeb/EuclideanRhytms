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
		m.items = InstFactory.getInstruments;

		m.background_(Color.fromHexString("30ECF8"));  // only changes the look of displayed item
		m.stringColor_(Color.black);   // only changes the look of displayed item
		m.font_(Font("AR DESTINE", 12));   // only changes the look of displayed item

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

		{ name == "kick808" } { instance = Kick808Inst.new }

		{ name == "snare" } { instance = SnareInst.new }

		{ name == "SOSsnare" } { instance = SOSsnareInst.new }

		{ name == "hi-hat" } { instance = HiHatInst.new }

		{ name == "sampler" }   { instance = SamplerInst.new }

		{ name == "cowbell" }   { instance = CowbellInst.new }

		{ name == "kalimba" }   { instance = KalimbaInst.new }

		{ name == "marimba" }   { instance = MarimbaInst.new }

		{ name == "SOShats"} { instance = SOShatsInst.new};

		instance.pdefId = this.prGenIstrumetId;
		^instance;

	}

	*getInstruments {
		^["SELECT INSTRUMENT","kick", "kick808", "snare", "SOSsnare", "hi-hat", "sampler", "cowbell", "kalimba", "marimba", "SOShats"];
	}

	//PRIVATE METHOD SHOULD NOT BE CALLED FROM OUTSIDE
	*prGenIstrumetId{
		this.instrumentId = this.instrumentId + 1;
		^this.instrumentId;
	}
}

Inst {

	classvar dim_knob_euclidean, dim_knob_sound;

	var sequence16, <>sequence, <>sequenceStraight, <>soundSource, <>paramSet, <>pdefId, level,
	    <>instLabel, muteBtn, <>closeBtn,
	    seqHitsKnob, seqLengthKnob, seqOffsetKnob, humanizerKnob, levelKnob, g;

	/*initializeSequencerGui INITIATES THE GUI RELATIVE TO A GENERIC INSTRUMENT*/
	initializeSequencerGui { | instView |

		var instView_width = instView.bounds.width;
		var instView_heigth = instView.bounds.height;

		//SETTING COMMON GUI DIMENSIONS & ELEMENTS
		dim_knob_euclidean = instView.bounds.width/5;
		paramSet = SoundParameters.new(instView);

		//SETTING INSTRUMENT LABEL
		instLabel = StaticText(instView, Rect(instView_width/4, instView_heigth/30, instView_width/2, instView_heigth/20));
		instLabel.font_("AR DESTINE", 35);
		instLabel.background = Color.fromHexString("BADFFF");
		instLabel.align_(\center);

		/*-----Euclidean Rhythm Controls--------*/


		g = ControlSpec.new(1, 16, \lin, 1);
		seqLengthKnob = EZKnob(instView,Rect(instView_width/9, instView_width*5/8, dim_knob_euclidean, dim_knob_euclidean),"length",g,initVal:16);

		seqLengthKnob.action_({
			sequence16 = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value); //in 16th notes
			sequenceStraight = EuclideanRhythmGen.convertToClockResolution(sequence16, 256);
			sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
			soundSource.quant_([sequence.size(),0,0,1]);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqHitsKnob = EZKnob(instView,Rect(instView_width*4/9, instView_width*5/8, dim_knob_euclidean, dim_knob_euclidean),"hits",g,initVal:0);

		seqHitsKnob.action_({
			sequence16 = EuclideanRhythmGen.compute_rhythm(seqHitsKnob.value, seqLengthKnob.value); //in 16th notes
			sequenceStraight = EuclideanRhythmGen.convertToClockResolution(sequence16, 256);
			sequence = RhythmEditor.humanize(sequenceStraight, 256, humanizerKnob.value);
			this.updateSequence(sequence);
		});

		g = ControlSpec.new(0, 16, \lin, 1);
		seqOffsetKnob = EZKnob(instView,Rect(instView_width*7/9, instView_width*5/8, dim_knob_euclidean, dim_knob_euclidean),"offset",g,initVal:0);

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
		humanizerKnob = EZKnob(instView,Rect(instView_width/9, instView_width*7/8, dim_knob_euclidean, dim_knob_euclidean),"humanizer",g,initVal:0);

		humanizerKnob.action_({
		});

		/*--------LEVEL CONTROL-----*/

		g = ControlSpec.new(0, 1.5, \lin, 0.1);
		levelKnob = EZKnob(instView,Rect(instView_width*4/9, instView_width*7/8, dim_knob_euclidean, dim_knob_euclidean),"level",g,initVal:0.5);

		levelKnob.action_({
			soundSource.set(\amp, levelKnob.value);
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

	var instView, decayKnob, punchKnob, compKnob;

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
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		//DECAY
		decayKnob = super.paramSet.addParameter(name:"decay", minVal:0.1, maxVal:8, initVal:1);
		decayKnob.action_({
			super.soundSource.set(\decay,decayKnob.value);
		});
		//PUNCH
		punchKnob = super.paramSet.addParameter(name:"punch", minVal:0, maxVal:1, initVal:0.55);
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

	var instView, snareDecayKnob, snareToneKnob;

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
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		//DECAY
		snareDecayKnob = super.paramSet.addParameter(name:"decay", minVal:0.05, maxVal:1, initVal:0.1);
		snareDecayKnob.action_({
			super.soundSource.set(\decay,snareDecayKnob.value);
		});
		//TONE
		snareToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.7, maxVal:1.3, initVal:1);
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

SOSsnareInst : Inst {

	var instView, snare909DecayKnob, snare909ToneKnob;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "SOS snare";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \SOSsnare,
				//\level, Pseq([0,0,0,0], inf)
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))> 0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		//DECAY
		snare909DecayKnob = super.paramSet.addParameter(name:"decay", minVal:0.3, maxVal:4, initVal:1);
		snare909DecayKnob.action_({
			super.soundSource.set(\decay, snare909DecayKnob.value);
		});
		//TONE
		snare909ToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.1, maxVal:4, initVal:1);
		snare909ToneKnob.action_({
			super.soundSource.set(\tone, snare909ToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{| sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \SOSsnare,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

HiHatInst : Inst {

	var instView, hi_hatDecayKnob, hi_hatToneKnob,
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
				\instrument, \hihat,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0,1,Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		//DECAY
		hi_hatDecayKnob = super.paramSet.addParameter(name:"decay", minVal:0.05, maxVal:1, initVal:0.1);
		hi_hatDecayKnob.action_({
			super.soundSource.set(\decay,hi_hatDecayKnob.value);
		});
		//TONE
		hi_hatToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.83, maxVal:1.6, initVal:1);
		hi_hatToneKnob.action_({
			super.soundSource.set(\tone,hi_hatToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \hihat,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}

}

SamplerInst : Inst {

	var instView, samplerLoadBtn, sample_rate_knob, b, synth,
	g;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "sampler";

		/* ---- btnLoad -----*/
		samplerLoadBtn = Button(instView, Rect(instView.bounds.left + (instView.bounds.width/2), instView.bounds.top + 150, templateView.bounds.width/5, templateView.bounds.width/20));
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
		sample_rate_knob = super.paramSet.addParameter(name:"rate", minVal:0, maxVal:3, initVal:0.5);

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
		super.soundSource.remove;
	}

	muteStrategy{ | muteBtn |
		if(muteBtn.value == 1, {synth.set(\mute, 0)},{synth.set(\mute, 1)});
	}

}

CowbellInst : Inst {

	var instView, cowbellToneKnob;

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
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		cowbellToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.83, maxVal:1.6, initVal:1);
		cowbellToneKnob.action_({
			super.soundSource.set(\fund_freq, 240+(cowbellToneKnob.value*300));
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

KalimbaInst : Inst {

	var instView, kalimbaToneKnob, kalimbaHarmonicity;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "kalimba";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \kalimba,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		kalimbaToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.5, maxVal:2, initVal:1);
		kalimbaToneKnob.action_({
			super.soundSource.set(\freqMod, kalimbaToneKnob.value);
		});

		kalimbaHarmonicity = super.paramSet.addParameter(name:"timbre", minVal:0.1, maxVal:0.5, initVal:0.1);
		kalimbaHarmonicity.action_({
			super.soundSource.set(\mix, kalimbaHarmonicity.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \kalimba,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

MarimbaInst : Inst {

	var instView, marimbaToneKnob, marimbaReleaseKnob;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "marimba";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \marimba,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		marimbaToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.5, maxVal:2, initVal:1);
		marimbaToneKnob.action_({
			super.soundSource.set(\freqMod, marimbaToneKnob.value);
		});

		marimbaReleaseKnob = super.paramSet.addParameter(name:"release", minVal:0.25, maxVal:2, initVal:1);
		marimbaReleaseKnob.action_({
			super.soundSource.set(\releaseMod, marimbaReleaseKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \marimba,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

SOShatsInst : Inst {

	var instView, soshatsToneKnob, soshatsDecayKnob;

		/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "SOShats";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \SOShats,
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))>0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		soshatsToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.5, maxVal:2, initVal:1);
		soshatsToneKnob.action_({
			super.soundSource.set(\tone, soshatsToneKnob.value);
		});

		soshatsDecayKnob = super.paramSet.addParameter(name:"decay", minVal:0.25, maxVal:2, initVal:1);
		soshatsDecayKnob.action_({
			super.soundSource.set(\decay, soshatsDecayKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{ | sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \SOShats,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

Kick808Inst : Inst {

	var instView, kick808DecayKnob, kick808ToneKnob;

	/*CREATES THE INTRUMENT SPECIFIC GUI COMPONENTS, AFTER CREEATING THE COMPOSITE VIEW FOR THIS INSTRUMENT*/
	createView{ | templateView |

		instView = CompositeView.new(templateView, Rect(0, 0, templateView.bounds.width, templateView.bounds.height) );
		instView.background = Color.grey;

		super.initializeSequencerGui(instView);

		/*---------------INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

		super.instLabel.string = "kick808";

		super.soundSource = Pdef(super.pdefId,
			Pbind(
				\instrument, \kick_808,
				//\level, Pseq([0,0,0,0], inf)
				\noteOrRest, Pif((Pseq(Array.fill(1024, {0}), inf))> 0, 1, Rest)
			)
		);
		super.soundSource.quant_([1024,0,0,1]);
		super.soundSource.set(\amp, 0.5);
		super.soundSource.play;

		/*---------------------SOUND PARAMETERS-----------------------------*/
		//DECAY
		kick808DecayKnob = super.paramSet.addParameter(name:"decay", minVal:0.7, maxVal:1.4, initVal:1);
		kick808DecayKnob.action_({
			super.soundSource.set(\decay, kick808DecayKnob.value);
		});
		//TONE
		kick808ToneKnob = super.paramSet.addParameter(name:"tone", minVal:0.5, maxVal:1.5, initVal:1);
		kick808ToneKnob.action_({
			super.soundSource.set(\tone, kick808ToneKnob.value);
		});

		/*-----------END INSTRUMENT-SPECIFIC GUI ELEMENTS--------------------*/

	}

	/*UPDATES THE SEQUENCE WHEN AN EUCLIDEAN PARAMETER IS UPDATED*/
		updateSequence{| sequence |
		var seq = Pseq(sequence, inf);
		Pdef(super.pdefId,
			Pbind(
				\instrument, \kick_808,
				\noteOrRest, Pif(seq > 0, 1, Rest())
			)
		);
		}

	removePdef{
		Pdef(super.pdefId).remove;
	}
}

//A set of parameters for the instrument (SynthDefs loaded by the SoundsLoader)
SoundParameters {

	classvar paramSet,
	         dim_knob;

	*new{ arg instView;

		paramSet = CompositeView( instView, Rect( 0, (instView.bounds.height/5), instView.bounds.width, (instView.bounds.height/5)));
		paramSet.background = Color.fromHexString("30ECF6");
		paramSet.addFlowLayout(margin:(instView.bounds.width/5)@20, gap:(instView.bounds.width/7)@0);

		dim_knob = instView.bounds.width/5;

		^super.new;
	}

	addParameter{ | name, minVal, maxVal, initVal, stepSize |

		var knob,cntrlSpec;

		cntrlSpec = ControlSpec.new(minVal, maxVal, \lin);
		knob = EZKnob(paramSet, Rect(0, 0, dim_knob, dim_knob),name, cntrlSpec, initVal:initVal);

		^knob;
	}

}
