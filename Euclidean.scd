/*-------------------------SOUND SOURCES-----------------------------------*/
(
//-KICK
SynthDef("kick", {arg freq = 60, t_gate = 0, decay = 1, punch = 0.5;
	var kick, ampEnv, impact;
	ampEnv = EnvGen.ar(Env.perc(0.01, decay, 2),	gate:t_gate);
	impact = EnvGen.kr(Env.perc(0, 0.4, punch*5), gate:t_gate);
	kick = SinOsc.ar(freq*impact, 0, ampEnv);
	kick = LPF.ar(kick, 300);
	Out.ar(0, [kick, kick]);
}).add;


//-808_COWBELL
SynthDef(\cowbell, { arg amp=0.3, t_gate=0, fund_freq=540, pan=0.0;
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
)

/*----------------------------GUI-----------------------------------------*/
(
var w = Window("Kick", Rect(100,100,250,400));
//backgound image....not working
//var a = Image.new(SCDoc.helpSourceDir +/+ "GUI Background.jpg");
//w.view.backgroundImage_(a);

//KICK GUI
var kickPlayBtn, decayKnob, punchKnob, km,
//COWBELL GUI
cowPlayBtn, cow_freq_knob, cow;

/*-----------------------------KICK---------------------------------------*/
km = Synth("kick");

/* ---- btnPlay -----*/
kickPlayBtn = Button(w, Rect(20, 160, 210, 30));
kickPlayBtn.string = "play";

kickPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	km.set(\t_gate, 1);
});
/* ---- Kick_Decay_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
decayKnob = EZKnob(w,Rect(10,10,100,100),"decay",g,initVal:0.5);

decayKnob.action_({
	km.set(\decay,decayKnob.value);
});
/* ---- Kick_Punch_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
punchKnob = EZKnob(w,Rect(130,10,100,100),"punch",g,initVal:0.5);

punchKnob.action_({
	km.set(\punch,punchKnob.value);
});
/*--------------------------END KICK---------------------------------------*/
/*--------------------------COWBELL----------------------------------------*/
cow = Synth("cowbell");

/* ---- btnPlay -----*/
cowPlayBtn = Button(w, Rect(20, 355, 210, 30));
cowPlayBtn.string = "play";

cowPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	cow.set(\t_gate, 1);
});
/* ---- Freq_Decay_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
cow_freq_knob = EZKnob(w,Rect(10,240,100,100),"tone", g,initVal:0.5);

cow_freq_knob.action_({
	cow.set(\fund_freq,240+(cow_freq_knob.value*700));
});

/*--------------------------END COWBELL------------------------------------*/
w.front;

w.onClose={km.free;cow.free;};
)


/*-------------------------END GUI-----------------------------------------*/

