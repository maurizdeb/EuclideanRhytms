SoundsLoader {

	*load{

		SynthDef(\reverb, { arg outBus = 0, inBus=0, pan = 0, mix=0, room=0.5;
			var input,out;

			input = In.ar(inBus, 2);

			out = FreeVerb.ar(input,mix:mix,room:room);
			Out.ar(outBus, Pan2.ar(out, pan));
		}).add;

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

		//SOSsnare
		SynthDef(\SOSsnare,
			{arg out = 0, sustain = 0.1, drum_mode_level = 0.25,
				snare_level = 40, snare_tightness = 1000,
				freq = 405, amp = 0.5, mute = 1, tone = 1, decay = 1;
				var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix, drum_mode_env;
				var snare_noise, snare_brf_1, snare_brf_2, snare_brf_3, snare_brf_4, snare_reson;
				var snare_env;
				var snare_drum_mix;

				drum_mode_env = EnvGen.ar(Env.perc(0.005, sustain*decay), 1.0, doneAction: 2);
				drum_mode_sin_1 = SinOsc.ar(freq*0.53*tone, 0, drum_mode_env * 0.5);
				drum_mode_sin_2 = SinOsc.ar(freq*tone, 0, drum_mode_env * 0.5);
				drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.85*tone),
					184,
					0.5/1.3,
					mul: drum_mode_env*5,
					add: 0);
				drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc]) * drum_mode_level;

				// choose either noise source below
				//	snare_noise = Crackle.ar(2.01, 1);
				snare_noise = LFNoise0.ar(20000, 0.1);
				snare_env = EnvGen.ar(Env.perc(0.005, sustain*decay), 1.0, doneAction: 2);
				snare_brf_1 = BRF.ar(in: snare_noise, freq: 8000, mul: 0.5, rq: 0.1);
				snare_brf_2 = BRF.ar(in: snare_brf_1, freq: 5000, mul: 0.5, rq: 0.1);
				snare_brf_3 = BRF.ar(in: snare_brf_2, freq: 3600, mul: 0.5, rq: 0.1);
				snare_brf_4 = BRF.ar(in: snare_brf_3, freq: 2000, mul: snare_env, rq: 0.0001);
				snare_reson = Resonz.ar(snare_brf_4, snare_tightness, mul: snare_level) ;
				snare_drum_mix = Mix.new([drum_mode_mix, snare_reson]) * 5 * amp;
				Out.ar(out, Pan2.ar(snare_drum_mix, 0, amp*mute))
			}
		).add;


		//HI-HAT
		SynthDef(\hihat, {arg out = 0, amp = 0.5, att = 0.01, rel = 0.2, ffreq = 6000, pan = 0, decay = 1, tone = 1, mute = 1;
			var env, snd;
			env = Env.perc(att, rel*decay, amp).kr(doneAction: 2);
			snd = WhiteNoise.ar;
			snd = HPF.ar(in: snd, freq: ffreq*tone, mul: env);
			Out.ar(out, Pan2.ar(snd, pan, amp*mute));
		}).add;


		//SAMPLER
		SynthDef(\sampler, { arg buf, rate=1, amp=0.1, t_gate=0, pan=0.0, mute=1;

			var sample;

			sample = PlayBuf.ar(2, buf, rate, t_gate);

			Out.ar(0, Pan2.ar(sample, pan, amp*mute));

		}).add;


		//-808_COWBELL
		SynthDef(\cowbell, { arg mute=1, amp=0.5, fund_freq=540, pan=0.0;
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

		//KALIMBA
		SynthDef(\kalimba, { arg mute = 1, amp = 0.5, freq = 440, freqMod = 1, mix = 0.1, relMin = 2.5, relMax = 3.5;

			//Kalimba based on bank of resonators
			var snd;
			// Basic tone is a SinOsc
			snd = SinOsc.ar(freq*freqMod) * EnvGen.ar(Env.perc(0.005, Rand(relMin, relMax), 1, -8), doneAction: 2);
			// The "clicking" sounds are modeled with a bank of resonators excited by enveloped pink noise
			snd = (snd * (1 - mix)) + (DynKlank.ar(`[
				// the resonant frequencies are randomized a little to add variation
				// there are two high resonant freqs and one quiet "bass" freq to give it some depth
				[240*ExpRand(0.9, 1.1), 2020*ExpRand(0.9, 1.1), 3151*ExpRand(0.9, 1.1)],
				[-7, 0, 3].dbamp,
				[0.8, 0.05, 0.07]
			], PinkNoise.ar * EnvGen.ar(Env.perc(0.001, 0.01))) * mix);
			Out.ar(0, Pan2.ar(snd, 0, amp*mute));
		}).add;

		//MARIMBA
		SynthDef(\marimba, {arg mute = 1, amp = 0.5, freq = 440, releaseMod = 1, freqMod = 1;
			var snd, env;
			env = Env.linen(0.015, 1, 0.5*releaseMod, amp).kr(doneAction: 2);
			snd = BPF.ar(Saw.ar(0), freq*freqMod, 0.02);
			snd = BLowShelf.ar(snd, 220, 0.81, 6);
			snd = snd * env;
			Out.ar(0, Pan2.ar(snd, 0, amp*mute));
		}).add;

		//SOShats
		SynthDef(\SOShats,
			{arg out = 0, freq = 6000, sustain = 0.1, amp = 0.5, decay = 1, tone = 1, mute = 1;
				var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
				var initial_bpf_contour, initial_bpf, initial_env;
				var body_hpf, body_env;
				var cymbal_mix;

				root_cymbal_square = Pulse.ar(freq*tone, 0.5, mul: 1);
				root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
					[freq*1.34*tone, freq*2.405*tone, freq*3.09*tone, freq*1.309*tone],
					[310/1.3, 26/0.5, 11/3.4, 0.72772],
					mul: 1,
					add: 0);
				root_cymbal = Mix.new(root_cymbal_pmosc);
				initial_bpf_contour = Line.kr(15000, 9000, 0.1);
				initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
				initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);
				body_env = EnvGen.ar(Env.perc(0.005, sustain*decay, 1, -2), 1.0, doneAction: 2);
				body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, sustain),mul: body_env, add: 0);
				cymbal_mix = Mix.new([initial_bpf, body_hpf]) * amp;
				Out.ar(out, Pan2.ar(cymbal_mix, 0, amp*mute));
		}).add;

		//KICK-808
		SynthDef(\kick_808, {arg out = 0, freq1 = 240, freq2 = 60, amp = 0.5, ringTime = 10, rel = 1, dist = 0.5, pan = 0, tone = 1, decay = 1, mute = 1;
			var snd, env;
			snd = Ringz.ar(
				in: Impulse.ar(0), // single impulse
				freq: XLine.ar(freq1*tone, freq2*tone, 0.1),
				decaytime: ringTime);
			env = EnvGen.ar(Env.perc(0.001, rel*decay, amp), doneAction: 2);
			snd = (1.0 - dist) * snd + (dist * (snd.distort));
			snd = snd * env;
			Out.ar(0, Pan2.ar(snd, pan, amp*mute));
		}).add;
	}
}
