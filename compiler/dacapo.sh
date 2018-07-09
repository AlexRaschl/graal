#!/bin/bash



# make all dirs
# ===================================================
mkdir bench_dacapo

# remember revision
git log -n 100 > rev.txt
cp rev.txt bench_dacapo

rm rev.txt

# loop lower and upper bound
counter="1"
maxCounter="4"

while [ $counter -le $maxCounter ]
do
  # dacapo baseline
    mx benchmark dacapo-timing:fop  -- --jvm-config=graal-core --jvm=server
    mv bench-results.json bench_dacapo/results_default_$counter.json
    
    # no inline config
    mx benchmark dacapo-timing:fop  -- --jvm-config=graal-core --jvm=server -Dgraal.Inline=false
    mv bench-results.json bench_dacapo/results_no_inline_$counter.json
    ((counter++))
done




# Zip All
# ===================================================
zip -r benchmarks bench_dacapo

# Delete rest
# ===================================================
rm -rf bench_dacapo
