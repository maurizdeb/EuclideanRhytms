(
SynthDef("kick", {arg freq = 60, t_gate = 0, decay = 1, punch = 0.5;
	var kick, ampEnv, impact;
	ampEnv = EnvGen.ar(Env.perc(0.01, decay, 2),	gate:t_gate);
	impact = EnvGen.kr(Env.perc(0, 0.4, punch*5), gate:t_gate);
	kick = SinOsc.ar(freq*impact, 0, ampEnv);
	kick = LPF.ar(kick, 300);
	Out.ar(0, [kick, kick]);
}).add;
)

(
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

~cow = Synth.new(\cowbell);
~cow.set(\t_gate, 1);

x = Synth.new(\kick);
x.set(\t_gate,1);
x.free;

TempoClock.default.tempo.postln;
TempoClock.default.tempo = 200/60
s.freeAll;

(
a = Pseq(#[1,0,0,0,1,0,0,0], inf).asStream;
r = Routine({
	loop {
		x.set(\t_gate, a.next);
		0.2.wait;
	}
});
)

(
b = Pseq(#[1,0,1,0,0,1,0,1], inf).asStream;
j = Routine({
	loop {
		~cow.set(\t_gate, b.next);
		0.2.wait;
	}
});
)



r.play;
r.stop;
r.value(TempoClock.default.tempo);
r.free;

j.play;
j.stop;
j.free;

s.freeAll;
s.plotTree;











(
SynthDef(\help_sinegrain,
    { arg out=0, freq=440, sustain=0.05;
        var env;
        env = EnvGen.kr(Env.perc(0.01, sustain, 0.2), doneAction: Done.freeSelf);
        Out.ar(out, SinOsc.ar(freq, 0, env))
    }).add;
)

(
a = Pseq(#[60, 61, 63, 65, 72], inf).asStream;
Routine({
    loop({
        Synth(\help_sinegrain, [\freq, a.next.midicps]);
		a.next.midicps.postln;
        0.2.wait;
    })
}).play;
)


















(
r = Routine { arg inval;
    loop {
        // thisThread refers to the routine.
        postf("beats: % seconds: % time: % \n",
            thisThread.beats, thisThread.seconds, Main.elapsedTime
        );
        1.0.yield;

    }
}.play;
)

r.stop;
r.beats;
r.seconds;
r.clock;