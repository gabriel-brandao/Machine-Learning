package arquivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Arquivo {
    private String nomeArquivo;
    
    public Arquivo (String nomeArquivo){
        this.nomeArquivo = nomeArquivo;
    }
    
    public void write(String texto) throws IOException{
        File arquivo = new File(nomeArquivo);
        
        if(!arquivo.exists())
            arquivo.createNewFile();
                                                    //false
        try (FileWriter fw = new FileWriter(arquivo, true); BufferedWriter bw = new BufferedWriter(fw)) {
            
            bw.write(texto);
            bw.newLine();
            
            bw.close();
            fw.close();   
        }
    }
    
    public void read() throws FileNotFoundException, IOException{
        File arquivo = new File(nomeArquivo);
        
        try (FileReader fr = new FileReader(arquivo); BufferedReader br = new BufferedReader(fr)) {
            
            while(br.ready()){
                String linha = br.readLine();
                System.out.println(linha);
            }
            
            br.close();
            fr.close();
        }
    }

}
