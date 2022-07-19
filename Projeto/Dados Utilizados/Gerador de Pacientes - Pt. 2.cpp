#include <iostream>
#include <string>
#include <string.h>
#include <fstream> //arquivo
#include <sstream> //to_string
#include <stdlib.h>
#include <time.h>
#include <vector>

#define NUMPACIENTE 10000

using namespace std;

int numero = 0;
int pesoPredicao;


int baixo = 0, medio = 0, alto = 0;
                                                ///ponteiro para generico
template < typename Type > string to_string (const Type& tipo){
  ostringstream retorno;
  retorno << tipo;
  return retorno.str();
}

string rp(){
    return to_string(++numero);
}

string idade (){
    int idade;
        //srand(time(NULL));
        idade = (rand() % 74) + 17; ///rand de 17 - 90

        if(idade >= 50)
            pesoPredicao += 2;

    return to_string(idade);
}

string sexo(){

    if((rand() % 10) % 2 == 0)
        return "masculino";
    else
        return "feminino";
}

string tabagismo() {

    switch(rand() % 3){
        case 0:
            pesoPredicao += 3;
            return "ex-fumante";
        case 1:
            pesoPredicao -= 4;
            return "nunca fumou";
        case 2:
            pesoPredicao += 4;
            return "fumante";
    }

}

string bebidasP_Semana(){
    int bebidas = (rand() % 7) + 1;

        if(bebidas > 2)
            pesoPredicao += 2;
        else
            pesoPredicao -= 2;

    return to_string(bebidas);
}

string gramasDeSalP_Semana(){

    int null_naonull = rand() % 10;
    float gramas;

    if(null_naonull < 4) ///40% de nao ter registro
        return "";
     else{
        gramas = (30 + (rand() % 701))/10.0;

        if(gramas > 35.0)
            pesoPredicao += 3;
        else
            pesoPredicao -= 3;

        return to_string(gramas);
     }
}

string pressaoArterial(){
    float pressao = (630 + (rand() % 1183)) / 100.0;

    if(pressao < 9.6 || pressao > 12.8)
        pesoPredicao += 3;

    return to_string(pressao);
}

string imc(){
    float imc = ((150 + (rand() % 280))) / 10.0;

    if(imc < 18.6 || imc > 25.0)
        pesoPredicao += 2;

    return to_string(imc);
}

string dificuldadeAtividadesBasicas(){

    if((rand() % 10) % 2 == 0){
        pesoPredicao += 2;
        return "sim";
    }
    else
        return "nao";
}

string doencasAutoreferidas(){

    if((rand() % 10) % 2 == 0){
        pesoPredicao += 3;
        return "tem alguma";
    }
    else
        return "nenhuma";
}

string historicoDeDoencaCardiaca(){

    if((rand() % 10) % 2 == 0){
        pesoPredicao += 4;
        return "tem";
    }
    else
        return "nao tem";

}

string planoDeSaude(){

    switch(rand() % 9 + 1){
        case 1:
        case 2:
        case 3:
            return "Basico";
        case 4:
        case 5:
        case 6:
            return "Avancado";
        case 7:
        case 8:
        case 9:
            return "Nenhum";
    }
}

void geraPacientes(){
    //int pesoPredicao;

    ofstream listaPacientes;
    listaPacientes.open("lista de pacientes.txt", ios::app);

    if(!listaPacientes){
        cout << "erro no arquivo" << endl;
        abort();
    }

    srand(time(NULL));
    for(int i = 0; i < NUMPACIENTE; i++){

        string paciente;
        pesoPredicao = 0;

        paciente += rp() + ",";
        paciente += idade() + ",";
        paciente += sexo() + ",";
        paciente += tabagismo() + ",";
        paciente += bebidasP_Semana() + ",";
        paciente += gramasDeSalP_Semana() + ","; ///pode ser NULL
        paciente += pressaoArterial() + ",";
        paciente += imc() + ",";
        paciente += dificuldadeAtividadesBasicas() + ",";
        paciente += doencasAutoreferidas() + ",";
        paciente += historicoDeDoencaCardiaca() + ",";
        paciente += planoDeSaude() + ",";

        if(pesoPredicao >= -7  && pesoPredicao <= 7){
            paciente += "BAIXO";
            baixo ++;
        }
         else
            if(pesoPredicao >= 8 && pesoPredicao <= 15){
               paciente += "MEDIO";
               medio ++;
            }
             else{
                paciente += "ALTO";
                alto ++;
             }

        cout << paciente << endl;
        listaPacientes << paciente << '\n';
    }

    listaPacientes.close();
}


double converteP_Double(string gramasStr){

    stringstream ss(gramasStr); //Temos agora uma string stream para processar a string "123"
    double gramas = 0.0;
    ss >> gramas;

    return gramas;
}

double recortaGramas(const string& paciente, char delimitador=',') {
    stringstream ss(paciente);
    string tok;
    string gramasStr;

    int nVirgula = 0;

    while (getline(ss, tok, delimitador)) {

        nVirgula ++;
        if(nVirgula == 6){
            if(tok.empty())
                gramasStr = "0";
            else
                gramasStr = tok;
            break;
        }
    }
    return converteP_Double(gramasStr);
}

void calcularMediaNull(){
    string paciente;
    double gramas = 0.0;

    int i = 0;

    ifstream listaPacientes; //para leitura
    listaPacientes.open("lista de pacientes.txt", ios::in); //abre para leitura **ios

    if(!listaPacientes){
        cout << "erro no arquivo" << endl;
        abort();
    }

    while(!listaPacientes.eof()){
        getline(listaPacientes, paciente);
        gramas += recortaGramas(paciente);
    }
    cout << "A media de sal consumido é: " << gramas/10000 << endl;
    listaPacientes.close();
}

int main(){
    //geraPacientes();
    //cout << endl << "ALTO: " << alto << "   MEDIO: " << medio << "   BAIXO: " << baixo << endl;
    calcularMediaNull();

}

