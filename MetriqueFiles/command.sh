cd ~/UQAC/8INF957/PMD

FILE1=programSizeInLinesOfCodes
FILE2=documentation
FILE3=complexity

METRICS=${FILE3}

OUT_SUFFIXES=

~/pmd-bin-6.18.0/bin/run.sh pmd -no-cache -t 2 -R ./xml/${METRICS}.xml -d ../JBelote/src/ -f text > ./${METRICS}${OUT_SUFFIXES}
