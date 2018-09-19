(
s.waitForBoot{(
/*-----------------------SOUND SOURCES DEFINITIONS (SYNTHDEFS)--------------------------------*/

//-KICK
SynthDef("kick", {arg amp=0.5, freq=60, t_gate=0, decay=1, punch=0.5, pan=0.0;
	var kick, ampEnv, impact;

	ampEnv = EnvGen.kr(Env.adsr(0.01, 3, 0.5, decay));
	impact = EnvGen.kr(Env.perc(0, decay, punch*5), t_gate);
	kick = SinOsc.ar(freq*impact);
	kick = Mix(kick * ampEnv);
	kick = LPF.ar(kick, 300);

	Out.ar(0, Pan2.ar( kick, pan, amp ));
}).add;


//-SNARE
SynthDef(\snare, {arg t_gate=0, tone=1,  decay=0.1, amp=0.3, pan=0.0;
	var ampEnv, snare;

	ampEnv = EnvGen.kr(Env.perc(0.001, decay, 1, -5), gate: t_gate);
	snare = SinOsc.ar(tone*120) - WhiteNoise.ar(0.5, 0.5);
	snare = Mix( snare * ampEnv);

	Out.ar(0, Pan2.ar( snare, pan, amp ) );
}).add;


//HI-HAT
SynthDef(\hi_hat, { arg t_gate = 0, opening = 0.2, amp = 0.35, freq = 1, pan=0.0;

    var hat, ampEnv;

	hat = LPF.ar(SinOsc.ar(6000*freq, mul: 0.2) - PinkNoise.ar(1),6000);
    hat = HPF.ar(hat,2000);

	ampEnv = EnvGen.kr(Env.perc(0.001, opening, 1, -5), gate: t_gate);

    hat = (hat * ampEnv);

    Out.ar(0, Pan2.ar(hat, pan, amp) );
}).add;


//SAMPLER
SynthDef(\sampler, { arg buf, rate=1, amp=1, t_gate=0, pan=0.0;

	var sample;

	sample = PlayBuf.ar(2, buf, rate, t_gate, 1);

	Out.ar(0, Pan2.ar(sample, pan, amp));

}).add;


//-808_COWBELL
SynthDef(\cowbell, { arg amp=0.4, t_gate=0, fund_freq=540, pan=0.0;
    var cow, env;

    cow = Pulse.ar( fund_freq * [ 1, 1.5085 ], [ 0.565, 0.445 ], [ 0.4, 0.6 ] ).distort;
    env = EnvGen.ar(
		Env([ 0, 0.05, 1, 0.1, 0 ], [ 0.003, 0.002, 0.05, 0.5 ], [2, -4, -4, -4]),
		gate: t_gate,
        timeScale: [ 1.0, 1.5 ]
        );
    cow = Mix( cow * env );
    cow = BPF.ar( cow, fund_freq * 2, 1.808 );

    Out.ar( 0, Pan2.ar( cow, pan, amp ) );
}).add;

)}
)

(

//GENERAL GUI VARIABLES
var w, w_width, w_height, screen_size,
    sidebar_width, instView_width, instGrid,
//TEMPO VARIABLES
    tempoKnob,
//INSTRUMENT VARIABLES
    kickInst, snareInst, hihatInst, samplerInst, cowbellInst;


w_width = 1024;
w_height = 950;
sidebar_width = 250;
instView_width = w_width - sidebar_width;

screen_size = Window.screenBounds;

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
tempoKnob = EZKnob(w, Rect(870, 10, 100, 100),"bpm", g, initVal:120);

tempoKnob.action_({
	var tmp = tempoKnob.value;
	TempoClock.default.tempo_( 4 * (tmp / 60)); //clock in 16th
	//tmp.postln;
});

/*----------------------------------INSTRUMENTS GRID--------------------------------------------*/

instGrid = CompositeView.new(w,Rect(0,1, instView_width, w_height) );
instGrid.background = Color.blue;
instGrid.decorator = FlowLayout(Rect(0, 0, instView_width, w_height) ); // adds a Grid layout for all the instuments

/*-----------------------------------INSTUMENTS GUI----------------------------------------------------*/

kickInst = KickInst.new;
kickInst.soundSource = Synth("kick");
kickInst.createView(instGrid, instView_width, w_width);

snareInst = SnareInst.new;
snareInst.soundSource = Synth("snare");
snareInst.createView(instGrid, instView_width, w_width);

hihatInst = HiHatInst.new;
hihatInst.soundSource = Synth("hi_hat");
hihatInst.createView(instGrid, instView_width, w_width);

samplerInst = SamplerInst.new;
samplerInst.soundSource = Synth("sampler");
samplerInst.createView(instGrid, instView_width, w_width);

/*cowbellInst = CowbellInst.new;
cowbellInst.soundSource = Synth("cowbell");
cowbellInst.createView(instGrid, instView_width, w_width);*/


/*--------------------------------END INSRTUMENTS GUI----------------------------------------------------*/

//instGrid.decorator.nextLine;

w.front;

w.onClose={s.freeAll; Pdef.removeAll};

)

