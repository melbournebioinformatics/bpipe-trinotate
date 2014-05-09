A bpipe script to run the [Trinotate](http://trinotate.sourceforge.net) transcriptome annotation pipeline

## Files Required

- A Trinity assembly file, `Trinity.fasta`
- A prepared pfam database

	```bash
	wget https://dl.dropboxusercontent.com/u/226794/Pfam-A.hmm.gz
	gunzip Pfam-A.hmm.gz
	hmmpress Pfam-A.hmm
	```

## To Run

```bash
module load bpipe
bpipe trinotate.groovy Trinity.fasta
```