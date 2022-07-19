package paciente;

public class Paciente {
    
    private int rp;
    private int idade;
    private String sexo;
    private String tabagismo;
    private int bebidasPorSemana;
    private Double gramasSalPorSemana;
    private double pressaoArterial;
    private double imc;
    private String dificuldadeAtvBasicas;
    private String doencasAutoreferidas;
    private String historicoDoencasCardio;
    private String planoSaude;
    private String risco;
    
    
    public Paciente (int rp,int idade, String sexo, String tabagismo, int bebidasPorSemana, 
            Double gramasSalPorSemana, double pressaoArterial, double imc, String dificuldadeAtvBasicas,
            String doencasAutoreferidas,String historicoDoencasCardio, String planoSaude, String risco){
    
            this.rp = rp;
            this.idade = idade;
            this.sexo = sexo;
            this.tabagismo = tabagismo;
            this.bebidasPorSemana = bebidasPorSemana;
            this.gramasSalPorSemana = gramasSalPorSemana;
            this.pressaoArterial = pressaoArterial;
            this.imc = imc;
            this.dificuldadeAtvBasicas = dificuldadeAtvBasicas;
            this.doencasAutoreferidas = doencasAutoreferidas;
            this.historicoDoencasCardio = historicoDoencasCardio;
            this.planoSaude = planoSaude;
            this.risco = risco;
    
    }
    
    public int getRp(){
        return rp;
    }
    
    public int getIdade(){
        return idade;
    }
    
    public String getSexo(){
        return sexo;
    }
    
    public String getTabagismo(){
        return tabagismo;
    }
    
    public int getBebidasPorSemana(){
        return bebidasPorSemana;
    }
    
    public Double getGramasSalPorSemana(){
        return gramasSalPorSemana;
    }
    
    public double getPressaoArterial(){
        return pressaoArterial;
    }
    
    public double getImc(){
        return imc;
    }
    
    public String getDificuldadeAtvBasicas (){
        return dificuldadeAtvBasicas;
    }
    
    public String getDoencasAutoreferidas (){
        return doencasAutoreferidas;
    }
    
    public String getHistoricoDoencasCardio (){
        return historicoDoencasCardio;
    }
    
    public String getPlanoSaude (){
        return planoSaude;
    }
    
    public String getRisco (){
        return risco;
    }
    
    public String to_String() {
        
        String string = getRp() + "," +
                        getIdade() + "," +
                        getSexo() + "," +
                        getTabagismo() + "," +
                        getBebidasPorSemana() + "," +
                        getGramasSalPorSemana() + "," +
                        getPressaoArterial() + "," +
                        getImc() + "," +
                        getDificuldadeAtvBasicas() + "," +
                        getDoencasAutoreferidas() + "," +
                        getDoencasAutoreferidas() + "," +
                        getHistoricoDoencasCardio() + "," +
                        getPlanoSaude() + "," +
                        getRisco();
        return string;
    }
}
