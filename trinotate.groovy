load 'bpipe.config'

SWISSPROTDB="/usr/local/blastdb/swissprot"
TRINOTATE_HOME="/usr/local/trinotate/r20131110/"
TRINITY_HOME="/usr/local/trinityrnaseq/r20131110-gcc/"
RNAMMER_PATH="/usr/local/rnammer/1.2/bin/rnammer"

transdecoder = {
	transform("fasta.transdecoder.pep","fasta.transdecoder.cds","fasta.transdecoder.bed","fasta.transdecoder.gff3"){
		exec """
		module load trinotate

		TransDecoder -t $input.fasta -m 100 --search_pfam Pfam-A.hmm --CPU 16
		"""
	}
}

blastx = {
	exec """
	module load blast+/2.2.29

	module load blastdb

	blastx -query $input.fasta -db $SWISSPROTDB -num_threads 16 -max_target_seqs 1 -outfmt 6 > $output
	"""
}

blastp = {
	exec """
	module load blast+/2.2.29

	module load blastdb;

	blastp -query $input.pep -db $SWISSPROTDB -num_threads 16 -max_target_seqs 1 -outfmt 6 > $output
	"""
}

hmmscan = {
	produce("hmmscan.out"){
	exec """
	module load trinotate

	hmmscan --cpu 16 --domtblout hmmscan.out Pfam-A.hmm $input.pep > pfam.log
	"""
	}	
}

signalp = {
	produce("signalp.out"){
		exec """
		module load trinotate

		signalp -f short -n signalp.out $input.pep
		"""
	}
}
	
tmhmm = {
	produce("tmhmm.out"){
		exec """
		module load trinotate

		tmhmm --short < $input.pep > tmhmm.out
		"""
	}
}

rnammmer = {
	produce(input.prefix+".rnammer.gff"){
		exec """
		module load trinotate

		$TRINOTATE_HOME/util/rnammer_support/RnammerTranscriptome.pl --transcriptome $input.fasta --path_to_rnammer $RNAMMER_PATH
		"""
	}
}

run { transdecoder+[blastx,blastp,hmmscan,signalp,tmhmm,rnammmer] }
