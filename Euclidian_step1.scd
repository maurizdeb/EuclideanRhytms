(
s.waitForBoot{

SynthDef("kick", {arg freq = 60, t_gate = 0, decay = 1, punch = 0.5;
	var kick, ampEnv, impact;
	ampEnv = EnvGen.ar(Env.perc(0.01, decay, 2),	gate:t_gate, doneAction:0);
	impact = EnvGen.kr(Env.perc(0, 0.4, punch*5), gate:t_gate);
	kick = SinOsc.ar(freq*impact, 0, ampEnv);
	kick = LPF.ar(kick, 300);
	Out.ar(0, [kick, kick]);
}).add;
}
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
TempoClock.default.tempo = 80/60;
s.freeAll;

(
a = Pseq(#[1,1,1,1,1,1,1,1,1], inf).asStream;
r = Routine({
	loop {
		x.set(\t_gate, a.next);
		1.wait; //da capire la dur
	}
});
) //Routine del kick

(
b = Pseq(#[1,0,1,0,0,1,0,1], inf).asStream;
j = Routine({
	loop {
		~cow.set(\t_gate, b.next);
		0.2.wait;
	}
});
) //routine del cowbell



r.play;
r.stop;
r.value(TempoClock.default.tempo);
r.free;

j.play;
j.stop;
j.free;

s.freeAll;
s.plotTree;

c = TempoClock(60/60);
Pdef(\kick).play(c,quant:4);
(
Pdef(\kick, Pbind(
	\type, \set,
	\id, x,
	\instrument, \kick,
	\args, #[\t_gate],
    \t_gate, Pseq([1, 0, 0, 0, 0, 0, 0, 0], inf),
)).stop;
)

(
p = Pbind(
	\type, \set,
	\id, x,
	\instrument, \kick,
	\args, #[\t_gate],
    \t_gate, Pseq([1, 0, 0, 0, 1, 0, 0, 0], inf),
);
)

p.play/*(quant:1/16)*/;
p.free;
p.stop;

x.free;

Pdef.removeAll;