/*FOR SOME MYSTICAL REASON CLASS METHODS DO NOT ACCEPT THE SYNTAX nameOfFunction.value( args... )
  BUT INSTEAD THEY ASK FOR A SYNTAX LIKE: nameOfFunction( args... )
*/

EuclideanRhythmGen {

	*compute_rhythm{ | num_hits, length |

		var divisor, level, remainder, count, sequence, flag = 0, k, n;

		k = num_hits;
		n = length;

		remainder = Array.new;
		count = Array.new;
		sequence = Array.new;

		if((k >= n) || (k == 0), {
			sequence = Array.fill(n, {0});
			if(k == n, {
				sequence = Array.fill(n, {1});
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
			sequence = this.build_string(sequence, level, count, remainder, flag);
			//sequence.postln;
		});

		if(flag == 0, {
			sequence = sequence.reverse;
		});

		^sequence;
	}

	*build_string { | sequence, level, count, remainder, flag |

		if(level == -1 , {
			sequence = sequence ++ [flag] //add 0 if k <= n/2;
		}, {
			if(level == -2 , {
				sequence = sequence ++ [1-flag];
			}, {
				for(0, count[level]-1, {
					sequence = this.build_string(sequence, level-1, count, remainder, flag);
				});
				if(remainder[level] != 0, {
					sequence = this.build_string(sequence, level-2, count, remainder, flag);
				});
			});
		});

		^sequence;
	}

}
