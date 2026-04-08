# Projeto Compilador Jack

Este repositório é dedicado ao desenvolvimento de um compilador para a linguagem *Jack*, conforme proposto na disciplina de Compiladores.

*Curso:* Engenharia da Computação - CCET - UFMA
*Professor:* Sergio Souza Costa

## 👥 Equipe
* *Paulo Eduardo Lima Rabelo*
  * Matrícula: 20260001203
* *Italo Francisco Almeida de Oliveira*
  * Matrícula: 20260001230 

## 🛠️ Tecnologias Utilizadas
- **Linguagem:** Java 17+
- **IDE:** Eclipse

## ▶️ Como Compilar e Executar

### Compilar
```bash
javac src/*.java -d out/
```

### Executar com um arquivo .jack
```bash
java -cp out JackAnalyzer caminho/para/arquivo.jack
```

### Executar com uma pasta inteira
```bash
java -cp out JackAnalyzer caminho/para/pasta/
```

O arquivo `.xml` gerado será salvo na mesma pasta do `.jack`.

## 🧪 Como Rodar os Testes

Os testes utilizam JUnit 5. No Eclipse, basta abrir o arquivo `JackScannerTest.java` e executar com **Run As → JUnit Test**.

## 📁 Estrutura do Projeto
```
src/
├── JackAnalyzer.java      # Ponto de entrada
├── JackScanner.java       # Analisador léxico
├── Token.java             # Modelo de token
├── TokenType.java         # Tipos de token
├── XMLGenerator.java      # Geração do XML de saída
└── JackScannerTest.java   # Testes unitários
```

## 📌 Observações
- A saída XML é compatível com o validador oficial do nand2tetris
- O repositório será reutilizado nas próximas unidades (Parser, Compilation Engine)