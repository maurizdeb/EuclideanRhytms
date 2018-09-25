(
s.waitForBoot{(
/*-----------------------SOUND SOURCES DEFINITIONS (SYNTHDEFS)--------------------------------*/
	//-KICK
	/*SynthDef("kick", {arg amp=0.2, freq=60, t_gate=0, decay=1, punch=0.5, level = 1, pan=0.0;

		var kick, ampEnv, impact;

		ampEnv = EnvGen.ar(Env.adsr(0.01, 3, 0.5, decay), doneAction:2);
		impact = EnvGen.ar(Env.perc(0, decay, punch*5));
		kick = SinOsc.ar(freq*impact);
		kick = Mix(kick * ampEnv);
		kick = LPF.ar(kick, 300);
		//kick = kick*1.2;
		//kick = kick.clip2(1);

		kick = kick.tanh;

		Out.ar(0, Pan2.ar( kick, pan, amp*level));
	}).add;*/


	SynthDef(\kick, { arg out = 0, pan = 0, amp = 0.2, punch = 0.5, decay = 0.3, mute=1;
		var body, bodyFreq, bodyAmp,
		    pop, popFreq, popAmp,
		    click, clickAmp,
		    kick;

		// body starts midrange, quickly drops down to low freqs, and trails off
		bodyFreq = EnvGen.ar(Env([522*punch, 120, 51], [0.035, 0.08], curve: \exp));
		bodyAmp = EnvGen.ar(Env.linen(0.005, 0.1, 1*decay), doneAction:2);
		body = SinOsc.ar(bodyFreq) * bodyAmp;
		// pop sweeps over the midrange
		popFreq = EnvGen.kr(Env([1500*punch, 261], [0.02], \exp));
		popAmp = EnvGen.ar(Env.perc(0.001, 0.07*decay)) * 0.15;
		pop = SinOsc.ar(popFreq) * popAmp;
		// click is spectrally rich, covering the high-freq range
		// you can use Formant, FM, noise, whatever
		clickAmp = EnvGen.ar(Env.perc(0.001, 0.01)) * 0.15;
		click = LPF.ar(Formant.ar(910, 4760, 2110), 3140) * clickAmp;

		kick = body + pop + click;
		kick = kick.tanh;

		Out.ar(out, Pan2.ar(kick, pan, amp*mute));
	}).add;



	//-SNARE
	SynthDef(\snare, {arg t_gate=0, tone=1,  decay=0.1, amp=0.2, pan=0.0, mute=1;
		var ampEnv, snare;

		ampEnv = EnvGen.kr(Env.perc(0.001, decay, 1, -5), doneAction:2);
		snare = SinOsc.ar(tone*120) - WhiteNoise.ar(0.5, 0.5);
		snare = Mix( snare * ampEnv);

		Out.ar(0, Pan2.ar( snare, pan, amp*mute ) );
	}).add;


	//HI-HAT
	SynthDef(\hi_hat, { arg t_gate = 0, opening = 0.2, amp = 0.2, freq = 1, pan=0.0, mute=1;

		var hat, ampEnv;

		hat = LPF.ar(SinOsc.ar(6000*freq, mul: 0.2) - PinkNoise.ar(1),6000);
		hat = HPF.ar(hat,2000);

		ampEnv = EnvGen.kr(Env.perc(0.001, opening, 1, -5), doneAction:2);

		hat = (hat * ampEnv);

		Out.ar(0, Pan2.ar(hat, pan, amp*mute) );
	}).add;


	//SAMPLER
	SynthDef(\sampler, { arg buf, rate=1, amp=0.2, t_gate=0, pan=0.0, mute=1;

		var sample;

		sample = PlayBuf.ar(2, buf, rate, 1, doneAction:2);

		Out.ar(0, Pan2.ar(sample, pan, amp*mute));

	}).add;


	//-808_COWBELL
	SynthDef(\cowbell, { arg amp=0.2, t_gate=0, fund_freq=540, pan=0.0, mute=1;
		var cow, env;

		cow = Pulse.ar( fund_freq * [ 1, 1.5085 ], [ 0.565, 0.445 ], [ 0.4, 0.6 ] ).distort;
		env = EnvGen.ar(
			Env([ 0, 0.05, 1, 0.1, 0 ], [ 0.003, 0.002, 0.05, 0.5 ], [2, -4, -4, -4]),
			timeScale: [ 1.0, 1.5 ],
			doneAction:2
		);
		cow = Mix( cow * env );
		cow = BPF.ar( cow, fund_freq * 2, 1.808 );

		Out.ar( 0, Pan2.ar( cow, pan, amp*mute ) );
	}).add;
)}
)

(

//GENERAL GUI VARIABLES
var w, w_width, w_height, screen_size, prop_height, prop_width,
    sidebar_width, instView_width, instGrid, instScroll,
//TEMPO VARIABLES
    tempoKnob,
//INSTRUMENT VARIABLES
    slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8;

screen_size = Window.screenBounds;

prop_width = 0.53;
prop_height = 0.88;
w_width = (prop_width * screen_size.width).round;
w_height = (prop_height * screen_size.height).round;
sidebar_width = (0.2 * w_width).round;
instView_width = w_width - sidebar_width;

w = Window("Euclidean AlgoRhythmic Sequencer", Rect(
	(screen_size.width /2) - (w_width/2),
	(screen_size.height /2) - (w_height/2) ,
	w_width,
	w_height));
w.alwaysOnTop = true;                         //For Developing purposes, it's better not to swap back and forth from code to window

//backgound image....not working
//var a = Image.new(SCDoc.helpSourceDir +/+ "GUI Background.jpg");
//w.view.backgroundImage_(a);


/*-----------------------------------------TEMPO------------------------------------------------*/

g = ControlSpec.new(40, 240, \linear, 1);
tempoKnob = EZKnob(w, Rect((0.85*w_width).round, (0.01*w_height).round, (prop_height*w_height*0.105).round, (prop_height*0.105*w_height).round),"bpm", g, initVal:120);

tempoKnob.action_({
	var tmp = tempoKnob.value;
	TempoClock.default.tempo_( 4 * (tmp / 60)); //clock in 16th
	//tmp.postln;
});

/*----------------------------------INSTRUMENTS GRID--------------------------------------------*/

instGrid = ScrollView.new(w,Rect(0,0, instView_width, w_height) );
instGrid.background = Color.fromHexString("4BA4B8");
instGrid.decorator = FlowLayout(Rect(0, 0, instView_width, w_height) ); // adds a Grid layout for all the instuments

/*-----------------------------------INSTUMENTS GUI----------------------------------------------------*/

slot1 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot2 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot3 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot4 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot5 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot6 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot7 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );
slot8 = TemplateInstGUI.new.create( instGrid, instView_width, w_width, w_height, prop_height );



/*--------------------------------END INSRTUMENTS GUI----------------------------------------------------*/

//instGrid.decorator.nextLine;

w.front;

w.onClose={s.freeAll; Pdef.removeAll};

)

