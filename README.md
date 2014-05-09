A bpipe script to run the [Trinotate](http://trinotate.sourceforge.net) transcriptome annotation pipeline

## Files Required

- A Trinity assembly file, `Trinity.fasta`
- A prepared pfam database

	```bash
	wget http://sourceforge.net/projects/trinotate/files/TRINOTATE_RESOURCES/Pfam-A.hmm.gz/download -O Pfam-A.hmm.gz
	gunzip Pfam-A.hmm.gz
	hmmpress Pfam-A.hmm
	```

## To Run

```bash
module load bpipe
bpipe trinotate.groovy Trinity.fasta
```