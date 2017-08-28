package cacheSimulator;

public class MemBlock {
	private int valor[];
	private int dirtyBit[];
	
MemBlock(int tamBloco){
		tamBloco = tamBloco/2;
		valor = new int [tamBloco];
		dirtyBit = new int [tamBloco];
		for (int i=0; i<tamBloco; i++){
			valor[i] = 0;
			dirtyBit[i] = 0;
		}
	}
}
