/*--------------------------GLOBAL SETTINGS--------------------------------*/


/*-------------------------SOUND SOURCES-----------------------------------*/
(
//-KICK
SynthDef("kick", {arg amp=0.4, freq=60, t_gate=0, decay=1, punch=0.5, pan=0.0;
	var kick, ampEnv, impact;

	ampEnv = EnvGen.kr(Env.adsr(0.01, 3, 0.5, decay),	gate:t_gate);
	impact = EnvGen.kr(Env.perc(0, decay, punch*5), gate:t_gate);
	kick = SinOsc.ar(freq*impact);
	kick = kick * ampEnv;
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
));

~snareSeq = Pdef(
	\snareSeq,
	Pbind(
		\type, \set,
		\id, ~snare,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq([0, 0, 0, 0, 1, 0, 0, 0], inf),
));


~cowbellSeq = Pdef(
	\cowbellSeq,
	Pbind(
		\type, \set,
		\id, ~cowbell,
		\instument, \snare,
		\args, #[\t_gate],
		\t_gate, Pseq([1, 1, 1, 0, 0, 0, 1, 0], inf),
));

)

/*----------------------------END SEQUENCES-------------------------------*/

/*----------------------------BJORKLUND ALGORITHM-------------------------*/
(
~compute_rhythm = {

	arg num_hits, length;
	var divisor, level, remainder, count, seq, result, flag = 0, k, n;

	k = num_hits;
	n = length;

	remainder = Array.new;
	count = Array.new;
	seq = Array.new;

	if(k >= n, {
		result = Array.fill(n, {0});
		if(k == n, {
			result = Array.fill(n, {1});
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

		remainder.postln;
		count.postln;

		count = count.insert(level, divisor);

		count.postln;

		if(flag == 0, {
			result = ~build_string.value(seq, level, count, remainder);
		});
		if(flag == 1, {
			result = ~build_string_inverted.value(seq, level, count, remainder);
		});
		result.postln;
		result.reverse;
	});
};
)

(
~build_string = {
	arg bitmap, level, count, remainder;

	if(level == -1 , {
		bitmap = bitmap ++ [0];
	}, {
		if(level == -2 , {
			bitmap = bitmap ++ [1];
		}, {
			for(0, count[level]-1, {
				bitmap = ~build_string.value(bitmap, level-1, count, remainder);
			});
			if(remainder[level] != 0, {
				bitmap = ~build_string.value(bitmap, level-2, count, remainder);
			});
		});
	});
	bitmap;
}
)

(
~build_string_inverted = {
	arg bitmap, level, count, remainder;

	if(level == -1 , {
		bitmap = bitmap ++ [1];
	}, {
		if(level == -2 , {
			bitmap = bitmap ++ [0];
		}, {
			for(0, count[level]-1, {
				bitmap = ~build_string_inverted.value(bitmap, level-1, count, remainder);
			});
			if(remainder[level] != 0, {
				bitmap = ~build_string_inverted.value(bitmap, level-2, count, remainder);
			});
		});
	});
	bitmap;
}
)

(~compute_rhythm.value(10,8);)

/*----------------------------END BJORKLUND ALGORITHM---------------------*/

/*----------------------------GUI-----------------------------------------*/
(

//GENERAL GUI VARIABLES
var w, w_width, w_height, screen_size,
//TEMPO VARIABLES
tempoKnob,
//KICK VARIABLES
kickPlayBtn, decayKnob, punchKnob,
//SNARE VARIABLES
snarePlayBtn,snareDecayKnob,
//COWBELL VARIABLES
cowPlayBtn, cow_freq_knob, cow;

w_width = 550;
w_height = 600;
screen_size = Window.screenBounds;

w = Window("Euclidean AlgoRhythmic Sequencer", Rect(
	(screen_size.width /2) - w_width/2,
	(screen_size.height /2) - w_height/2 ,
	w_width,
	w_height));
w.alwaysOnTop = true;                         //For Developing purposes, it's better not to swap back and forth from code to window

//backgound image....not working
//var a = Image.new(SCDoc.helpSourceDir +/+ "GUI Background.jpg");
//w.view.backgroundImage_(a);


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
	~kickSeq.play(quant:4);

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
	~snareSeq.play(quant:4);
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
	~cowbellSeq.play(quant:4);
});

/* ---- Freq_Decay_Knob -----*/
g = ControlSpec.new(0, 1, \lin);
cow_freq_knob = EZKnob(w,Rect(10,240,100,100),"tone", g,initVal:0.5);

cow_freq_knob.action_({
	~cowbell.set(\fund_freq,240+(cow_freq_knob.value*700));
});

/*--------------------------END COWBELL------------------------------------*/
w.front;

w.onClose={s.freeAll; ~kickSeq.stop; ~cowbellSeq.stop; ~snareSeq.stop; ~kickSeq.remove; ~cowbellSeq.remove; ~snareSeq.remove;};
)


/*-------------------------END GUI-----------------------------------------*/

