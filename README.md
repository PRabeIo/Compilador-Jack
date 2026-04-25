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
* **Linguagem de Programação:** Java
* **Testes:** JUnit 5

## 📁 Estrutura do Projeto

```
CompiladorJack/
├── src/
│   ├── JackAnalyzer.java       # Ponto de entrada
│   ├── JackScanner.java        # Analisador léxico
│   ├── Token.java              # Representa um token
│   ├── TokenType.java          # Enum com os tipos de token
│   ├── XMLGenerator.java       # Gera o arquivo XML do scanner
│   ├── CompilationEngine.java  # Analisador sintático (parser)
│   ├── JackScannerTest.java    # Testes do scanner (JUnit 5)
│   └── ParserTest.java         # Testes do parser (JUnit 5)
├── tests/
│   ├── Scanner/                # Arquivos .jack para teste do scanner
│   │   ├── Main.jack
│   │   ├── Square.jack
│   │   └── SquareGame.jack
│   ├── Square/                 # Arquivos .jack para teste do parser
│   │   ├── Main.jack
│   │   ├── Square.jack
│   │   └── SquareGame.jack
│   ├── expected/               # XMLs oficiais do nand2tetris para validação
│   │   ├── MainT.xml
│   │   ├── SquareT.xml
│   │   ├── SquareGameT.xml
│   │   ├── MainP.xml
│   │   ├── SquareP.xml
│   │   └── SquareGameP.xml
│   └── output/                 # XMLs gerados pelo programa (criado automaticamente)
├── .project                    # Configuração do Eclipse
├── .classpath                  # Configuração do Eclipse
└── README.md
```

---

## ▶️ Como rodar no Eclipse

### Importar o projeto

1. Abra o Eclipse
2. `File` → `Import` → `General` → `Existing Projects into Workspace`
3. Selecione a pasta `CompiladorJack`
4. Clique em `Finish`

### Adicionar JUnit 5 ao projeto

1. Clique com botão direito no projeto → `Build Path` → `Add Libraries`
2. Selecione `JUnit` → `Next`
3. Escolha **JUnit 5** → `Finish`

### Rodar o JackAnalyzer

1. Clique com botão direito em `JackAnalyzer.java` → `Run As` → `Run Configurations`
2. Na aba **Arguments**, em **Program arguments**, coloque:
   ```
   tests/Square
   ```
3. Em **Working directory** → `Other` → `Workspace` → selecione a raiz do projeto
4. Clique em `Run`

O programa executa as duas etapas para cada arquivo `.jack` encontrado:
```
✓ [Scanner] Main.jack → output/MainT.xml
✓ [Parser]  Main.jack → output/MainP.xml
✓ [Scanner] Square.jack → output/SquareT.xml
✓ [Parser]  Square.jack → output/SquareP.xml
✓ [Scanner] SquareGame.jack → output/SquareGameT.xml
✓ [Parser]  SquareGame.jack → output/SquareGameP.xml
```

Todos os arquivos gerados ficam em `tests/output/`.

---

## 🧪 Como rodar os testes (JUnit 5)

### Rodar todos os testes

1. Clique com botão direito na pasta `src`
2. `Run As` → `JUnit Test`

### Rodar apenas os testes do scanner

1. Clique com botão direito em `JackScannerTest.java`
2. `Run As` → `JUnit Test`

### Rodar apenas os testes do parser

1. Clique com botão direito em `ParserTest.java`
2. `Run As` → `JUnit Test`

> ⚠️ Para os testes de validação oficial funcionarem, os arquivos `T.xml` e `P.xml` oficiais do nand2tetris devem estar em `tests/expected/`.

### Resultado esperado

```
Runs: 28/28   Errors: 0   Failures: 0
```

---

## 📌 Tokens Reconhecidos

| Tipo | Exemplos |
|------|---------|
| `keyword` | `class`, `int`, `while`, `return` |
| `symbol` | `{`, `}`, `+`, `-`, `<`, `>` |
| `integerConstant` | `0`, `14`, `444` |
| `stringConstant` | `"hello"`, `"Jack"` |
| `identifier` | `x`, `Square`, `moveUp` |

---

## 📄 Formato das Saídas XML

### Scanner (`XxxT.xml`)

```xml
<tokens>
  <keyword> class </keyword>
  <identifier> Main </identifier>
  <symbol> { </symbol>
  ...
</tokens>
```

### Parser (`XxxP.xml`)

```xml
<class>
  <keyword> class </keyword>
  <identifier> Main </identifier>
  <symbol> { </symbol>
  <subroutineDec>
    ...
  </subroutineDec>
</class>
```

Caracteres especiais são escapados em ambas as saídas:

| Caractere | Escape |
|-----------|--------|
| `&` | `&amp;` |
| `<` | `&lt;` |
| `>` | `&gt;` |
| `"` | `&quot;` |

---

## ✅ Status da Validação

| Arquivo | Scanner (T.xml) | Parser (P.xml) |
|---------|:-:|:-:|
| Main.jack | ✅ | ✅ |
| Square.jack | ✅ | ✅ |
| SquareGame.jack | ✅ | ✅ |

---

## 💬 Relato dos Desafios

O principal desafio foi a integração entre o scanner e o parser. O scanner gera tokens com tipos específicos, e o parser precisa consumir esses tokens respeitando estritamente a gramática Jack. Garantir que os tipos fossem reconhecidos corretamente em todas as regras da gramática, especialmente em `compileType` e `compileTerm`, exigiu bastante atenção.

Outro desafio foi acertar o formato exato do XML de saída — pequenas diferenças como indentação incorreta faziam os testes falharem, o que nos levou a implementar a normalização do XML antes da comparação, removendo espaços iniciais de cada linha para focar apenas na hierarquia de tags.

---


