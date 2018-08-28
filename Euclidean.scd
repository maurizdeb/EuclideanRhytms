/*--------------------------GLOBAL SETTINGS--------------------------------*/


/*-------------------------SOUND SOURCES-----------------------------------*/
(
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


/*----INSTANTIATING SOUNDS--------*/
~kick = Synth("kick");
~cowbell = Synth("cowbell");
~snare = Synth("snare");
~hi_hat = Synth("hi_hat");

)

/*----------------------------END SOUND SOURCES---------------------------*/
/*----------------------------BJORKLUND ALGORITHM-------------------------*/

(
~compute_rhythm = {

	arg num_hits, length;
	var divisor, level, remainder, count, seq, flag = 0, k, n;

	k = num_hits;
	n = length;

	remainder = Array.new;
	count = Array.new;
	seq = Array.new;

	if((k >= n) || (k == 0), {
		seq = Array.fill(n, {0});
		if(k == n, {
			seq = Array.fill(n, {1});
		});
	},{
		if(k > (n/2), {
			k = n - k;
			flag = 1;
		});

		divisor = n - k;
		level = 0;

		remainder = remainder.insert(0,k);
		count = count.insert(level,(divisor / remainder[level]).floor);
		remainder =  remainder.insert(level+1, divisor % remainder[level]); //% corresponds to mod(divisor, reminder[level])
		divisor = remainder[level];
		level = level +1;

		while( {remainder[level] > 1}, {
			count = count.insert(level,(divisor / remainder[level]).floor);
			remainder =  remainder.insert(level+1, divisor % remainder[level]); //% corresponds to mod(divisor, reminder[level])
			divisor = remainder[level];
			level = level +1;
		});

		//remainder.postln;
		//count.postln;

		count = count.insert(level, divisor);

		//count.postln;
		seq = ~build_string.value(seq, level, count, remainder, flag);
		//seq.postln;
	});
	if(flag == 0, {
		seq = seq.reverse;
	});
	seq;
};

~build_string = {
	arg bitmap, level, count, remainder, flag;

	if(level == -1 , {
		bitmap = bitmap ++ [flag] //add 0 if k <= n/2;
	}, {
		if(level == -2 , {
			bitmap = bitmap ++ [1-flag];
		}, {
			for(0, count[level]-1, {
				bitmap = ~build_string.value(bitmap, level-1, count, remainder, flag);
			});
			if(remainder[level] != 0, {
				bitmap = ~build_string.value(bitmap, level-2, count, remainder, flag);
			});
		});
	});
	bitmap;
}
)

/*----------------------------END BJORKLUND ALGORITHM---------------------*/

/*----------------------------GUI-----------------------------------------*/
(

//GENERAL GUI VARIABLES
var w, w_width, w_height, screen_size,
//TEMPO VARIABLES
tempoKnob,
//KICK VARIABLES
kickPlayBtn, decayKnob, punchKnob,
kickSeqHitsKnob, kickSeqLengthKnob,
//SNARE VARIABLES
snarePlayBtn,snareDecayKnob,snareToneKnob,
snareSeqHitsKnob, snareSeqLengthKnob,
//COWBELL VARIABLES
cowPlayBtn, cow_freq_knob,
cowbellSeqHitsKnob, cowbellSeqLengthKnob,
//HI-HAT VARIABLES
hi_hatPlayBtn, hi_hatToneKnob, hi_hatDecayKnob,
hi_hatSeqHitsKnob, hi_hatSeqLengthKnob;

w_width = 1024;
w_height = 800;
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


/*------------------------------TEMPO-------------------------------------*/
g = ControlSpec.new(40, 240, \linear, 1);
tempoKnob = EZKnob(w, Rect(720, 10, 100, 100),"bpm", g, initval:120);

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
	~kickPattern = ~compute_rhythm.value(2,8);
	~kickSeq.play(quant:4);

});
/* ---- Kick_Decay_Knob -----*/
g = ControlSpec.new(0, 3, \lin);
decayKnob = EZKnob(w,Rect(10,10,100,100),"decay",g,initVal:0.001);

decayKnob.action_({
	~kick.set(\decay,decayKnob.value);
});
/* ---- Kick_Punch_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
punchKnob = EZKnob(w,Rect(130,10,100,100),"punch",g,initVal:0.02);

punchKnob.action_({
	~kick.set(\punch,punchKnob.value);
});

/*-----Euclidean Rhythm Controls: Kick--------*/


g = ControlSpec.new(1, 16, \lin, 1);
kickSeqLengthKnob = EZKnob(w,Rect(250,10,80,80),"length",g,initVal:16);

kickSeqLengthKnob.action_({
	~kickPattern = ~compute_rhythm.value(kickSeqHitsKnob.value, kickSeqLengthKnob.value);
	~kickSeq = Pdef(
		\kickSeq,
		Pbind(
			\type, \set,
			\id, ~kick,
			\instument, \kick,
			\args, #[\t_gate],
			\t_gate, Pseq(~kickPattern, inf),
	));
});

g = ControlSpec.new(0, 16, \lin, 1);
kickSeqHitsKnob = EZKnob(w,Rect(360,10,80,80),"hits",g,initVal:0);

kickSeqHitsKnob.action_({
	~kickPattern = ~compute_rhythm.value(kickSeqHitsKnob.value, kickSeqLengthKnob.value);
	~kickSeq = Pdef(
		\kickSeq,
		Pbind(
			\type, \set,
			\id, ~kick,
			\instument, \kick,
			\args, #[\t_gate],
			\t_gate, Pseq(~kickPattern, inf),
	));

});

/*--------------------------END KICK---------------------------------------*/
/*-----------------------------SNARE---------------------------------------*/

/* ---- btnPlay -----*/
snarePlayBtn = Button(w, Rect(20, 540, 210, 30));
snarePlayBtn.string = "snr play";

snarePlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~snarePattern = ~compute_rhythm.value(11,13);
	~snareSeq.play(quant:4);
});

/* ---- Snare_Decay_Knob -----*/
g = ControlSpec.new(0.05, 1, \lin);
snareDecayKnob = EZKnob(w,Rect(10,425,100,100),"decay",g,initVal:0.1);

snareDecayKnob.action_({
	~snare.set(\decay,snareDecayKnob.value);
});

/* ---- Snare_Tone_Knob -----*/
g = ControlSpec.new(0.7, 1.3, \lin);
snareToneKnob = EZKnob(w,Rect(130,425,100,100),"tone",g,initVal:1);

snareToneKnob.action_({
	~snare.set(\tone,snareToneKnob.value);
});
/*-----Euclidean Rhythm Controls: snare--------*/


g = ControlSpec.new(1, 16, \lin, 1);
snareSeqLengthKnob = EZKnob(w,Rect(250,425,80,80),"length",g,initVal:16);

snareSeqLengthKnob.action_({
	~snarePattern = ~compute_rhythm.value(snareSeqHitsKnob.value, snareSeqLengthKnob.value);
	~snareSeq = Pdef(
	\snareSeq,
	Pbind(
		\type, \set,
		\id, ~snare,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq(~snarePattern, inf),
));
});

g = ControlSpec.new(0, 16, \lin, 1);
snareSeqHitsKnob = EZKnob(w,Rect(360,425,80,80),"hits",g,initVal:0);

snareSeqHitsKnob.action_({
	~snarePattern = ~compute_rhythm.value(snareSeqHitsKnob.value, snareSeqLengthKnob.value);
	~snareSeq = Pdef(
	\snareSeq,
	Pbind(
		\type, \set,
		\id, ~snare,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq(~snarePattern, inf),
));

});

/*--------------------------END SNARE---------------------------------------*/
/*--------------------------COWBELL----------------------------------------*/

/* ---- btnPlay -----*/
cowPlayBtn = Button(w, Rect(20, 355, 210, 30));
cowPlayBtn.string = "cwb play";

cowPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~cowbellSeq.play(quant:4);
});

/* ---- Freq_Decay_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
cow_freq_knob = EZKnob(w,Rect(10,240,100,100),"tone", g,initVal:0.5);

cow_freq_knob.action_({
	~cowbell.set(\fund_freq,240+(cow_freq_knob.value*700));
});
/*-----Euclidean Rhythm Controls: cowbell--------*/


g = ControlSpec.new(1, 16, \lin, 1);
cowbellSeqLengthKnob = EZKnob(w,Rect(250,240,80,80),"length",g,initVal:16);

cowbellSeqLengthKnob.action_({
	~cowbellPattern = ~compute_rhythm.value(cowbellSeqHitsKnob.value, cowbellSeqLengthKnob.value);
	~cowbellSeq = Pdef(
	\cowbellSeq,
	Pbind(
		\type, \set,
		\id, ~cowbell,
		\instument, \cowbell,
		\args, #[\t_gate],
		\t_gate, Pseq(~cowbellPattern, inf),
));
});

g = ControlSpec.new(0, 16, \lin, 1);
cowbellSeqHitsKnob = EZKnob(w,Rect(360,240,80,80),"hits",g,initVal:0);

cowbellSeqHitsKnob.action_({
	~cowbellPattern = ~compute_rhythm.value(cowbellSeqHitsKnob.value, cowbellSeqLengthKnob.value);
	~cowbellSeq = Pdef(
	\cowbellSeq,
	Pbind(
		\type, \set,
		\id, ~cowbell,
		\instument, \cowbell,
		\args, #[\t_gate],
		\t_gate, Pseq(~cowbellPattern, inf),
));

});

/*--------------------------END COWBELL------------------------------------*/

/*-----------------------------HI HAT---------------------------------------*/

/* ---- btnPlay -----*/
hi_hatPlayBtn = Button(w, Rect(20, 725, 210, 30));
hi_hatPlayBtn.string = "hi-hat play";

hi_hatPlayBtn.action_({ arg butt;
	butt.value.asBoolean.not.postln;
	~hi_hatPattern = ~compute_rhythm.value(11,13);
	~hi_hatSeq.play(quant:4);
});

/* ---- Hi-hat_Decay_Knob -----*/
g = ControlSpec.new(0.05, 1, \lin);
hi_hatDecayKnob = EZKnob(w,Rect(10,610,100,100),"decay",g,initVal:0.1);

hi_hatDecayKnob.action_({
	~hi_hat.set(\opening,hi_hatDecayKnob.value);
});

/* ---- Hi-hat_Tone_Knob -----*/
g = ControlSpec.new(0.83, 1.6, \lin);
hi_hatToneKnob = EZKnob(w,Rect(130,610,100,100),"tone",g,initVal:1);

hi_hatToneKnob.action_({
	~hi_hat.set(\freq,hi_hatToneKnob.value);
});
/*-----Euclidean Rhythm Controls: hi-hat--------*/


g = ControlSpec.new(1, 16, \lin, 1);
hi_hatSeqLengthKnob = EZKnob(w,Rect(250,610,80,80),"length",g,initVal:16);

hi_hatSeqLengthKnob.action_({
	~hi_hatPattern = ~compute_rhythm.value(hi_hatSeqHitsKnob.value, hi_hatSeqLengthKnob.value);
	~hi_hatSeq = Pdef(
	\hi_hatSeq,
	Pbind(
		\type, \set,
		\id, ~hi_hat,
		\instument, \hi_hat,
		\args, #[\t_gate],
		\t_gate, Pseq(~hi_hatPattern, inf),
));
});

g = ControlSpec.new(0, 16, \lin, 1);
hi_hatSeqHitsKnob = EZKnob(w,Rect(360,610,80,80),"hits",g,initVal:0);

hi_hatSeqHitsKnob.action_({
	~hi_hatPattern = ~compute_rhythm.value(hi_hatSeqHitsKnob.value, hi_hatSeqLengthKnob.value);
	~hi_hatSeq = Pdef(
	\hi_hatSeq,
	Pbind(
		\type, \set,
		\id, ~hi_hat,
		\instument, \hi_hat,
		\args, #[\t_gate],
		\t_gate, Pseq(~hi_hatPattern, inf),
));

});

/*--------------------------END HI-HAT---------------------------------------*/
w.front;

w.onClose={s.freeAll; ~kickSeq.stop; ~cowbellSeq.stop; ~snareSeq.stop; ~hi_hatSeq.stop;~kickSeq.remove; ~cowbellSeq.remove; ~snareSeq.remove; ~hi_hatSeq.remove};
)


/*-------------------------END GUI-----------------------------------------*/

