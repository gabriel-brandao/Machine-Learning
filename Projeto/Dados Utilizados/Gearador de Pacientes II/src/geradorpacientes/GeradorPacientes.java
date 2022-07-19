package geradorpacientes;

import arquivo.Arquivo;
import java.io.IOException;
import paciente.Paciente;
import java.util.Random;

public class GeradorPacientes {
    public static int baixo=0, medio=0, alto=0;
    
    public static int numPacientes = 3;
    public static int pesoPredicao;
    public static double mediaGramasSal = 0; 

    public static Random gerador = new Random();


    public static int idade() {
        int idade = gerador.nextInt(74) + 17; //17 - 90

        if (idade >= 50)
            pesoPredicao += 2;

        return idade;
    }

    public static String sexo() {

        if (gerador.nextBoolean())
            return "\"feminino\"";
        else
            return "\"masculino\"";
    }

    public static String tabagismo() {

        switch (gerador.nextInt(3)) {
            case 0:
                pesoPredicao += 3;
                return "\"ex-fumante\"";
            case 1:
                pesoPredicao -= 4;
                return "\"nunca fumou\"";
            case 2:
                pesoPredicao += 4;
                return "\"fumante\"";
        }
        return null;
    }

    public static int bebidasP_Semana() {
        int bebidas = gerador.nextInt(7) + 1; //1 - 7

        if (bebidas > 2)
            pesoPredicao += 2;
        else
            pesoPredicao -= 2;

        return bebidas;
    }

    public static Double gramasDeSalP_Semana() {
        int null_naonull = gerador.nextInt(10);
        double gramas;

        if (null_naonull < 4){ ///40% de nao ter registro
            mediaGramasSal += 0;
            return null;
        }
        else {
            gramas = (gerador.nextInt(730) + 30) / 10.0; //3g - 75g
            
            if (gramas > 35.0)
                pesoPredicao += 3;
            else
                pesoPredicao -= 3;
            
            mediaGramasSal += gramas;
            return gramas;
        }
    }

    public static double pressaoArterial() {
        double pressao = (gerador.nextInt(1183) + 630) / 100.0; //6.3 - 18.13

        if (pressao < 9.6 || pressao > 12.8)
            pesoPredicao += 3;

        return pressao;
    }

    public static double imc() {
       double imc = (gerador.nextInt(280) + 150) / 10.0; //15.0 - 42.9

        if (imc < 18.6 || imc > 25.0)
            pesoPredicao += 2;
        return imc;
    }

    public static String dificuldadeAtividadesBasicas() {

        if (gerador.nextBoolean()) {
            pesoPredicao += 2;
            return "\"sim\"";
        } else
            return "\"nao\"";
    }

    public static String doencasAutoreferidas() {

        if (gerador.nextBoolean()) {
            pesoPredicao += 3;
            return "\"tem alguma\"";
        } else
            return "\"nenhuma\"";
    }

    public static String historicoDeDoencaCardiaca() {

        if (gerador.nextBoolean()) {
            pesoPredicao += 4;
            return "\"tem\"";
        } else
            return "\"nao tem\"";
    }

    public static String planoDeSaude() {
        int saude = gerador.nextInt(3);

        switch (saude) {
            case 0:
                return "\"Basico\"";
            case 1:
                return "\"Avancado\"";
            case 2:
                return "\"Nenhum\"";
        }
        return null;
    }
    
    private static String risco() {
        if(pesoPredicao >= -9  && pesoPredicao <= 10){
            baixo ++;
            return "BAIXO";
        }
        
        if(pesoPredicao >= 11 && pesoPredicao <= 14){
               medio ++;
               return "MEDIO";
        }
         else{
              alto ++;
              return "ALTO";    
             } 
    }
    
    public static void geraPacientes() throws IOException {
        Paciente paciente = null;        
        Arquivo arquivo = new Arquivo("lista de pacientes.csv");
        
        arquivo.write("rp,idade,sexo,tabagismo,bebidasP/Sem,gDeSalP/Sem,"
                + "pressaoArterial,imc,dificAtvFisica,doençasAutoref,historico,planoSaude,risco");
        
        for (int rp = 0; rp < numPacientes; rp++) {
            pesoPredicao = 0;
            paciente = new Paciente(rp+1, idade(), sexo(), tabagismo(), bebidasP_Semana(),gramasDeSalP_Semana(), 
                                    pressaoArterial(), imc(), dificuldadeAtividadesBasicas(), 
                                    doencasAutoreferidas(), historicoDeDoencaCardiaca(), planoDeSaude(), risco());

            arquivo.write(paciente.to_String());
        }
    }

    public static void main(String[] args) throws IOException {
        geraPacientes();
        System.out.printf("MEDIA GRAMAS DE SAL P/ SEMANA: %.2f \n\n" , (mediaGramasSal/numPacientes));
        System.out.println("ALTO: " + alto + " MEDIO: " + medio + " BAIXO: " + baixo);
    }
}