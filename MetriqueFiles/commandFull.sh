cd ~/Documents/UQAC/8INF957/SessionBelote/MetriqueFiles
mkdir -p ./resultat2

METRICS=('badclass' 'classTooLenght' 'complexity' 'coupling' 'cycloComplexity' 'documentation' 'programSizeInLinesOfCodes')

OUT_SUFFIXES=
for i in `seq 0 6`;
do
	~/pmd-bin-6.18.0/bin/run.sh pmd -no-cache -t 2 -R ./xml/${METRICS[i]}.xml -d ../NewJBelote/src/ -f text > ./resultat2/${METRICS[i]}${OUT_SUFFIXES}
done
