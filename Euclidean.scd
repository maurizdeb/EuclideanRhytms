/*--------------------------GLOBAL SETTINGS--------------------------------*/


/*-------------------------SOUND SOURCES-----------------------------------*/
(
//-KICK
SynthDef("kick", {arg amp=0.3, freq=60, t_gate=0, decay=1, punch=0.5, pan=0.0;
	var kick, ampEnv, impact;

	ampEnv = EnvGen.ar(Env.adsr(0.01, 3, 0.5, decay),	gate:t_gate);
	impact = EnvGen.kr(Env.perc(0, decay, punch*5), gate:t_gate);
	kick = SinOsc.ar(freq*impact);
	kick = Mix(kick * ampEnv);
	kick = LPF.ar(kick, 300);

	Out.ar(0, Pan2.ar( kick, pan, amp ));
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


//-SNARE
SynthDef(\snare, {arg t_gate=0, decay=0.1, amp=0.3, pan=0.0;
	var ampEnv, snare;

	ampEnv = EnvGen.kr(Env.perc(0.001, decay, 1, -5), gate: t_gate);
	snare = SinOsc.ar(120) - WhiteNoise.ar(0.5, 0.5);
	snare = Mix( snare * ampEnv);

	Out.ar(0, Pan2.ar( snare, pan, amp ) );
}).add;

)
/*----------------------------SEQUENCES-----------------------------------*/
(
~kick = Synth("kick");
~cowbell = Synth("cowbell");
~snare = Synth("snare");

~kickSeq = Pdef(
	\kickSeq,
	Pbind(
		\type, \set,
		\id, ~kick,
		\instument, \kick,
		\args, #[\t_gate],
		\t_gate, Pseq([1, 0, 0, 0, 1, 0, 0, 0], inf),
)).stop;

~snareSeq = Pdef(
	\snareSeq,
	Pbind(
		\type, \set,
		\id, ~snare,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq([0, 0, 0, 0, 1, 0, 0, 0], inf),
)).stop;


~cowbellSeq = Pdef(
	\cowbellSeq,
	Pbind(
		\type, \set,
		\id, ~cowbell,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq([0, 0, 1, 0, 0, 0, 1, 0], inf),
)).stop;

)

/*----------------------------END SEQUENCES-------------------------------*/

/*----------------------------BJORKLUND ALGORITHM-------------------------*/

//If hits < length/2 ...
//else we need to swap 0 and ones...
//obvious solution must be computed differently

//testFunc.while( bodyFunc );

compute_rhythm = { arg num_hits, length;

	var divisor = length - num_hits;
	reminder[0] = num_hits;    //transform in supercollider code
	var level = 0;

	//riprendi da qua

};

//---------------------CODICE C DA CONVERTIRE
void function compute_bitmap (int num_slots, int num_pulses)
{
/*---------------------
* First, compute the count and remainder arrays
*/
divisor = num_slots – num_pulses;
remainder[0] = num_pulses;
level = 0;
do {
count[level] = divisor / remainder[level];
remainder[level+1] = mod(divisor, remainder[level]);
divisor = remainder[level];
level = level + 1;
while (remainder[level] > 1);
count[level] = divisor;
/*---------------------
* Now build the bitmap string
*/
build_string (level);
}

	void function build_string (int level)
{
if (level == -1)
append a “0” to the end of the bitmap;
else if (level == -2)
append a “1” to the end of the bitmap;
else {
for (i=0; i < count[level]; i++)
build_string (level-1);
if (remainder[level] != 0)
build_string (level-2);
}/*end else*/
}
//-----------------FINE CODICE C DA CONVERTIRE

/*----------------------------END BJORKLUND ALGORITHM---------------------*/

/*----------------------------GUI-----------------------------------------*/
(
var w = Window("Euclidean AlgoRhythmic Sequencer", Rect(100,100,550,600));
//backgound image....not working
//var a = Image.new(SCDoc.helpSourceDir +/+ "GUI Background.jpg");
//w.view.backgroundImage_(a);

//TEMPO GUI
var tempoKnob,
//KICK GUI
kickPlayBtn, decayKnob, punchKnob,
//SNARE GUI
snarePlayBtn,snareDecayKnob,
//COWBELL GUI
cowPlayBtn, cow_freq_knob, cow;

/*------------------------------TEMPO-------------------------------------*/
g = ControlSpec.new(40, 240, \linear, 1);
tempoKnob = EZKnob(w, Rect(400, 10, 100, 100),"bpm", g, initval:120);

tempoKnob.action_({
	var tmp = tempoKnob.value;
	TempoClock.default.tempo_( 4 * (tmp / 60)); //clock in 16th
	//tmp.postln;
});

/*------------------------------END TEMPO---------------------------------*/
/*-----------------------------KICK---------------------------------------*/

/* ---- btnPlay -----*/
kickPlayBtn = Button(w, Rect(20, 160, 210, 30));
kickPlayBtn.string = "kik play";

kickPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~kickSeq.play(quant:1);

});
/* ---- Kick_Decay_Knob -----*/
g = ControlSpec.new(0, 3, \lin);
decayKnob = EZKnob(w,Rect(10,10,100,100),"decay",g,initVal:1.5);

decayKnob.action_({
	~kick.set(\decay,decayKnob.value);
});
/* ---- Kick_Punch_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
punchKnob = EZKnob(w,Rect(130,10,100,100),"punch",g,initVal:0.5);

punchKnob.action_({
	~kick.set(\punch,punchKnob.value);
});
/*--------------------------END KICK---------------------------------------*/
/*-----------------------------SNARE---------------------------------------*/

/* ---- btnPlay -----*/
snarePlayBtn = Button(w, Rect(20, 540, 210, 30));
snarePlayBtn.string = "snr play";

snarePlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~snareSeq.play(quant:1);
});

/* ---- Snare_Decay_Knob -----*/
g = ControlSpec.new(0.05, 1, \lin);
snareDecayKnob = EZKnob(w,Rect(10,425,100,100),"decay",g,initVal:0.1);

snareDecayKnob.action_({
	~snare.set(\decay,snareDecayKnob.value);
});

/*--------------------------END SNARE---------------------------------------*/
/*--------------------------COWBELL----------------------------------------*/

/* ---- btnPlay -----*/
cowPlayBtn = Button(w, Rect(20, 355, 210, 30));
cowPlayBtn.string = "cwb play";

cowPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~cowbellSeq.play(quant:1);
});

/* ---- Freq_Decay_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
cow_freq_knob = EZKnob(w,Rect(10,240,100,100),"tone", g,initVal:0.5);

cow_freq_knob.action_({
	~cowbell.set(\fund_freq,240+(cow_freq_knob.value*700));
});

/*--------------------------END COWBELL------------------------------------*/
w.front;

w.onClose={s.freeAll; ~kickSeq.free; ~cowbellSeq.free; ~snareSeq.free;};
)


/*-------------------------END GUI-----------------------------------------*/

