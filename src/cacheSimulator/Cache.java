package cacheSimulator;

import static java.lang.Math.log;
import java.util.Random;

public class Cache {
	private static final int endereco = 32;  // endereçamento em bits
	private static final int auxCalcBits = 2147483647;
	private int auxBlock; // Bloco Auxiliar para Leituras que geram Escritas
	private int tamBloco;
	private int numConjuntos;
	private int assoc; 			 // 1= Direto 2= 2Way 4= 4Way ...
	private int bitsOffset;
	private int bitsIndice;
	private int bitsTag;
	private boolean val[][];
	private boolean dirtyBit[][];
	private int tag[][];
	private int nCompulsoryMiss;
	private int nCapacityMiss;
	private int nConflictMiss;
	private int nWriteMiss;
	private int nHit;
	private int nWrite;
	private int nRead;
	private int nCacheAcess;
	private static int nTotalAcess;
	
	// CONSTRUTOR
	Cache(int numConjuntos, int tamBloco, int assoc){
		this.tamBloco = tamBloco;
		this.numConjuntos = numConjuntos;
		this.assoc = assoc;
		calculaBits(this.tamBloco, this.numConjuntos, this.assoc);
		val   = new boolean[(this.numConjuntos/this.assoc)][this.assoc];
		dirtyBit   = new boolean[(this.numConjuntos/this.assoc)][this.assoc];
		tag   = new int[(this.numConjuntos/this.assoc)][this.assoc];
		
		for(int i=0; i<(this.numConjuntos/this.assoc); i++){
			for(int j=0; j<this.assoc; j++){
				val[i][j] = false;
				dirtyBit[i][j] = false;
				tag[i][j] = 0;
			}			
		}
	}
	
	private void calculaBits (int tamBloco, int numConjuntos, int assoc){
		this.bitsOffset = (int)(log(tamBloco/endereco) / log(2));
		this.bitsIndice = (int)(log(numConjuntos/assoc) / log(2));
		this.bitsTag = endereco - this.bitsOffset - this.bitsIndice;
		//System.out.println(this.bitsTag +" " +this.bitsIndice +" " +this.bitsOffset); // Imprime Quantidade de bits para cada parte do endereço
	}
	
	// Returns: -2 = Hit, -1 = Miss sem Dirty Bit, tag = Miss com Dirty Bit
	public boolean read (int endAtual){
		int offset = endAtual << this.bitsTag + this.bitsIndice - 1;
		offset = (offset & auxCalcBits) >>> this.bitsTag + this.bitsIndice -1;
		int index = endAtual << this.bitsTag - 1;
		index = (index & auxCalcBits) >>> this.bitsTag + this.bitsOffset -1;
		int tag = endAtual >>> this.bitsIndice + this.bitsOffset;
		
		this.nRead++;
		this.nCacheAcess++;
		nTotalAcess++;
		
		// Procura em todas as páginas no índice
		for (int i=0; i<this.assoc; i++){
			// Avalia Cache Hit
			if(this.tag[index][i] == tag && this.val[index][i] == true){
				this.nHit++;
				return false;  // Não precisa escrever na Cache
			}
		}
		
		Random rand = new Random();
		int blockToUse = rand.nextInt(this.assoc);  // Aloca o Bloco para ser usado
		
		//Avalia Bit de Validade do Bloco escolhido
		if (this.val[index][blockToUse] == false){
			this.nCompulsoryMiss++;
			this.auxBlock = blockToUse;
			return true;
		}
		else{
			this.nConflictMiss++;
			this.auxBlock = blockToUse;
			return true;
		} //System.out.println(tag +" " +index +" " +offset);*/
		
	}
	
	public int write (int endAtual, boolean status){
		int offset = endAtual << this.bitsTag + this.bitsIndice - 1;
		offset = (offset & auxCalcBits) >>> this.bitsTag + this.bitsIndice -1;
		int index = endAtual << this.bitsTag - 1;
		index = (index & auxCalcBits) >>> this.bitsTag + this.bitsOffset -1;
		int tag = endAtual >>> this.bitsIndice + this.bitsOffset;
		
		this.nWrite++;
		this.nCacheAcess++;
		nTotalAcess++;
		
		for (int i=0; i<this.assoc; i++){
			// Avalia Write Hit
			if(this.tag[index][i] == tag){
				this.val[index][i] = true;
				this.dirtyBit[index][i] = true;
				return -1;
			}
		}
		
		this.nWriteMiss++;
		
		int blockToUse;
		if (status){
			blockToUse = this.auxBlock;
		}
		else{
			Random rand = new Random();
			blockToUse = rand.nextInt(this.assoc);
		}
		
		// Avalia o Dirty Bit do bloco escolhido
		if (this.dirtyBit[index][blockToUse] == false){
			this.tag[index][blockToUse] = tag;
			this.dirtyBit[index][blockToUse] = true;
			this.val[index][blockToUse] = true;
			return -1;
		}
		else{
			int aux = (this.tag[index][blockToUse] << this.bitsIndice + this.bitsOffset) | (index << this.bitsOffset) | (blockToUse);
			this.tag[index][blockToUse] = tag;
			this.dirtyBit[index][blockToUse] = true;
			this.val[index][blockToUse] = true;
			return aux;
		}
	}
	
	public void printCacheReport (){
		System.out.println(
		  "Acessos de Leitura:             " +this.nRead +"\n"
		+ "Acessos de Escrita:             " +this.nWrite +"\n"
		+ "Total de Acessos a Cache:       " +this.nCacheAcess +"\n"
		+ "Total de Acertos:               " +this.nHit +"\n"
		+ "Total de Perdas Compulsórias:   " +this.nCompulsoryMiss +"\n"
		+ "Total de Perdas de Conflito:    " +this.nConflictMiss +"\n"
		+ "Total de Perdas de Capacidade:  " +this.nCapacityMiss +"\n"
		+ "Total de Perdas de Escrita:     " +this.nWriteMiss +"\n"
		+ "Total de Perdas de Capacidade:  " +this.nCapacityMiss +"\n"
		+ "Taxa de Acertos Local (%):      " +getHitRate(this.nCacheAcess) +"\n"
		+ "Taxa de Acertos Global(%):      " +getHitRate(nTotalAcess) +"\n"
		+ "Taxa de Perdas  Local (%):      " +getMissRate(this.nCacheAcess) +"\n"
		+ "Taxa de Perdas  Global(%):      " +getMissRate(nTotalAcess) +"\n");
	}
	
	private float getMissRate(int local){
		return (((float)this.nCompulsoryMiss + (float)this.nCapacityMiss + (float)this.nConflictMiss) / (float)local);
	}
	
	private float getHitRate(int local){
		return ((float)this.nHit / (float)local);
	}
}
