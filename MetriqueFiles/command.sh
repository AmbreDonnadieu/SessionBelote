cd ~/Documents/UQAC/8INF957/SessionBelote/MetriqueFiles
mkdir -p ./resultat

FILE1=programSizeInLinesOfCodes
FILE2=documentation
FILE3=complexity

METRICS=${FILE3}

OUT_SUFFIXES=

~/pmd-bin-6.18.0/bin/run.sh pmd -no-cache -t 2 -R ./xml/${METRICS}.xml -d ../JBelote/src/ -f text > ./resultat/${METRICS}${OUT_SUFFIXES}
