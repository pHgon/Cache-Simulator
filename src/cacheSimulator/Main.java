package cacheSimulator;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class Main {

	public static void main(String[] args) {
		final int threshold = 100;
		int nsets_L1i, bsize_L1i, assoc_L1i, nsets_L1d, bsize_L1d, assoc_L1d, nsets_L2, bsize_L2, assoc_L2;
		String arquivo_de_entrada;
		
		// Arquivo de Entrada sempre deve ser informado por par�metro
		// Se Nenhum Par�metro � passado, carrega Cache com configura��es Default
		if (args.length==0){
			System.out.println("Nenhum par�metro passado.. Criando Cache Default\n");
			nsets_L1i = 1024;
			bsize_L1i = 32;
			assoc_L1i = 1;
			nsets_L1d = 1024;
			bsize_L1d = 32;
			assoc_L1d = 1;
			nsets_L2  = 1024;
			bsize_L2  = 32;
			assoc_L2  = 1;
			arquivo_de_entrada = new String ("arqBinario1_rw_10k.dat");
		}
		// Cache carregada com configura��es passadas por par�metro
		else{
			nsets_L1i = Integer.parseInt(args[0]);
			bsize_L1i = Integer.parseInt(args[1]);
			assoc_L1i = Integer.parseInt(args[2]);
			nsets_L1d = Integer.parseInt(args[3]);
			bsize_L1d = Integer.parseInt(args[4]);
			assoc_L1d = Integer.parseInt(args[5]);
			nsets_L2  = Integer.parseInt(args[6]);
			bsize_L2  = Integer.parseInt(args[7]);
			assoc_L2  = Integer.parseInt(args[8]);
			arquivo_de_entrada = args[9];
		}
		
		// Inst�ncia Caches L1 de intru��es, L1 de Dados e L2
		Cache L1_i = new Cache(nsets_L1i, bsize_L1i, assoc_L1i);
		Cache L1_d = new Cache(nsets_L1d, bsize_L1d, assoc_L1d);
		Cache L2   = new Cache(nsets_L2,  bsize_L2,  assoc_L2);
		try{
			// Carrega Arquivo Bin�rio de Entrada
			InputStream arquivo = Main.class.getResourceAsStream(arquivo_de_entrada);
			DataInputStream input = new DataInputStream(arquivo);
			int readError = 0; // Contador de Erros, caso seja carregado do arquivo algum valor inv�lido

			do {
				int endAtual  = input.readInt();  // L� endere�o atual
				int infoAtual = input.readInt();  // L� informa��o atual
				if (endAtual < 0 || infoAtual < 0 || infoAtual > 1){  //Testa se algum valor � inv�lido
					readError++;
				}
				else{  // Faz Acesso a L1 de Dados ou Instru��es
					//System.out.println(infoAtual +" " +endAtual);
					int aux;
					boolean needWriteL1, needWriteL2;
					
					switch(infoAtual){  // 0 = read  1 = write
					case 0:
						if (endAtual < threshold){
							needWriteL1 = L1_d.read(endAtual);
							if (needWriteL1){
								needWriteL2 = L2.read(endAtual);
								if (needWriteL2){
									aux = L2.write(endAtual, true);
								}
								aux = L1_d.write(endAtual, true);
								if (aux >= 0){
									aux = L2.write(aux, false);
								}
							}
						}
						else{
							needWriteL1 = L1_i.read(endAtual);
							if (needWriteL1){
								needWriteL2 = L2.read(endAtual);
								if (needWriteL2){
									aux = L2.write(endAtual, true);
								}
								aux = L1_d.write(endAtual, true);
								if (aux >= 0){
									aux = L2.write(aux, false);
								}
							}
						}
						break;
					case 1:
						if (endAtual < threshold){
							int writeL2 = L1_d.write(endAtual, false);
							if (writeL2 >= 0){
								writeL2 = L2.write(writeL2, false);
							}
						}
						else{
							int writeL2 = L1_i.write(endAtual, false);
							if (writeL2 >= 0){
								writeL2 = L2.write(writeL2, false);
							}
						}
						break;
					}
				}
			} while (input.available() > 0);
			
			input.close();
			arquivo.close();
			
			// IMPRIME RELAT�RIO
			System.out.println("RELAT�RIO CACHE L1 DE INSTRU��ES");
			L1_i.printCacheReport();
			System.out.println("\nRELAT�RIO CACHE L1 DE DADOS");
			L1_d.printCacheReport();
			System.out.println("\nRELAT�RIO CACHE L2");
			L2.printCacheReport();
				
		}
		catch (FileNotFoundException ex) {
			System.out.println("Arquivo n�o encontrado");
		} 
		catch (IOException ex) {
			System.out.println("Problema na leitura do arquivo");
		}	
	}	
}