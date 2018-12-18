SoundsLoader {

	*load{

		SynthDef(\kick, { arg out = 0, pan = 0, amp = 0.5, punch = 0.5, decay = 0.3, mute=1;
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
		SynthDef(\snare, {arg tone=1,  decay=0.1, amp=0.5, pan=0.0, mute=1;
			var ampEnv, snare;

			ampEnv = EnvGen.kr(Env.perc(0.001, decay, 1, -5), doneAction:2);
			snare = SinOsc.ar(tone*120) - WhiteNoise.ar(0.5, 0.5);
			snare = Mix( snare * ampEnv);

			Out.ar(0, Pan2.ar( snare, pan, amp*mute ) );
		}).add;

		//SNARE909
		SynthDef(\snare909,{ |out=0, amp=1, mute=1, velocity=1, decay=1, tone=1|
			var excitation, membrane;

			excitation = LPF.ar(WhiteNoise.ar(1), 7040, 1) * (0.1 + velocity);
			membrane = (
				/* Two simple enveloped oscillators represent the loudest resonances of the drum membranes */
				(LFTri.ar(330*tone, 0, 1) * EnvGen.ar(Env.perc(0.0005,0.055*decay),doneAction:2) * 0.25)
				+(LFTri.ar(185*tone, 0, 1) * EnvGen.ar(Env.perc(0.0005,0.075*decay),doneAction:2) * 0.25)

				/* Filtered white noise represents the snare */
				+(excitation * EnvGen.ar(Env.perc(0.0005,0.4*decay),doneAction:2) * 0.2)
				+(HPF.ar(excitation, 523, 1) * EnvGen.ar(Env.perc(0.0005,0.283*decay),doneAction:2) * 0.2)

			) * amp;
			Out.ar(out, membrane!2, amp*mute);
		}).add;


		//HI-HAT
		SynthDef(\hi_hat, { arg opening = 0.2, amp = 0.5, freq = 1, pan=0.0, mute=1;

			var hat, ampEnv;

			hat = LPF.ar(SinOsc.ar(6000*freq, mul: 0.1) - PinkNoise.ar(1), 12000);
			hat = HPF.ar(hat,4000);

			ampEnv = EnvGen.kr(Env.perc(0.001, opening, 1, -5), doneAction:2);

			hat = (hat * ampEnv);

			Out.ar(0, Pan2.ar(hat, pan, amp*mute) );
		}).add;


		//SAMPLER
		SynthDef(\sampler, { arg buf, rate=1, amp=0.1, t_gate=0, pan=0.0, mute=1;

			var sample;

			sample = PlayBuf.ar(2, buf, rate, t_gate);

			Out.ar(0, Pan2.ar(sample, pan, amp*mute));

		}).add;


		//-808_COWBELL
		SynthDef(\cowbell, { arg amp=0.5, fund_freq=540, pan=0.0, mute=1;
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

	}
}

//Synth(\snare909).play;