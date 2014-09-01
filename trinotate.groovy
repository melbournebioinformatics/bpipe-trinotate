load 'bpipe.config'

// Although this script makes some pretense and allowing arbitrary names input files
// this doesn't really work.  Trinotate itself will fail unless certain files 
// have predefined names.  Always name your input file Trinity.fasta

// Edit these variables to suit your local system setup
// The settings below are appropriate for the LTU Cluster
//
NCPU="16"
SWISSPROTDB="\$BLASTDB/swissprot"
TRINOTATE_HOME="/usr/local/trinotate/r20140708/"
TRINITY_HOME="/usr/local/trinityrnaseq/r20140717-gcc/"
RNAMMER_PATH="/usr/local/rnammer/1.2/bin/rnammer"
PFAM_DB="/usr/local/pfamdb/Pfam-A.hmm"

transdecoder = {
	transform("fasta.transdecoder.pep","fasta.transdecoder.cds","fasta.transdecoder.bed","fasta.transdecoder.gff3"){
		exec """
		module load trinotate

		TransDecoder -t $input.fasta -m 100 --search_pfam $PFAM_DB --CPU $NCPU
		"""
	}
}

blastx = {
	exec """
	module load blast+/2.2.29

	module load blastdb

	blastx -query $input.fasta -db $SWISSPROTDB -num_threads $NCPU -max_target_seqs 1 -outfmt 6 > $output
	"""
}

blastp = {
	exec """
	module load blast+/2.2.29

	module load blastdb;

	blastp -query $input.pep -db $SWISSPROTDB -num_threads $NCPU -max_target_seqs 1 -outfmt 6 > $output
	"""
}

hmmscan = {
	produce("hmmscan.out"){
	exec """
	module load trinotate

	hmmscan --cpu $NCPU --domtblout hmmscan.out  $PFAM_DB $input.pep > pfam.log
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


// This stage invariably fails with exit code 999
rnammmer = {
	produce(input.prefix+".rnammer.gff"){
		exec """
		module load trinotate

		$TRINOTATE_HOME/util/rnammer_support/RnammerTranscriptome.pl --transcriptome $input.fasta --path_to_rnammer $RNAMMER_PATH
		"""
	}
}

download_sqlite_template = {
	exec """
	module load trinotate

	cp \$TRINOTATE_DB .
	"""
}

create_transmap = {
	exec """
	module load trinityrnaseq-gcc

	$TRINITY_HOME/util/support_scripts/get_Trinity_gene_to_trans_map.pl $input.fasta >  Trinity.fasta.gene_trans_map
	"""
}

load_data = {
	exec """
	module load trinotate

	Trinotate Trinotate.sqlite init --gene_trans_map Trinity.fasta.gene_trans_map --transcript_fasta Trinity.fasta --transdecoder_pep Trinity.fasta.transdecoder.pep

	Trinotate Trinotate.sqlite LOAD_swissprot_blastp Trinity.fasta.transdecoder.pep.blastp

	Trinotate Trinotate.sqlite LOAD_swissprot_blastx Trinity.fasta.blastx

	Trinotate Trinotate.sqlite LOAD_pfam hmmscan.out

	Trinotate Trinotate.sqlite LOAD_tmhmm tmhmm.out

	Trinotate Trinotate.sqlite LOAD_signalp signalp.out
	"""
}

load_rnammer = {
	exec """
	module load trinotate

	Trinotate Trinotate.sqlite LOAD_rnammer Trinity.fasta.rnammmer.gff
	"""
}

//run { transdecoder+[blastx,blastp,hmmscan,signalp,tmhmm] }
run { transdecoder+[blastx,blastp,hmmscan,signalp,tmhmm]+download_sqlite_template+create_transmap+load_data+rnammmer+load_rnammer}









