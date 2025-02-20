### **Sistema de Gestão para Salão de Beleza**

**Versão:** 1.0  
**Autor:** [Antônio Carlos](https://github.com/antonio-carlosjr)   
**Licença:** GPL  

---

## **Descrição**

O **SaloonProject** é um sistema desktop desenvolvido em **Java** para gerenciamento de salões de beleza e adaptável para barbearias. Ele automatiza processos administrativos, como controle de clientes, prestadores de serviço, cálculo de comissões e geração de relatórios.  

A interface gráfica foi construída utilizando **Swing**, com um banco de dados **MySQL** para armazenamento das informações.  

---
![{CC83237C-9D65-4472-BA32-FD3AE8E4CF92}](https://github.com/user-attachments/assets/6a3124f5-6fd6-437c-b4cb-cdb7dfba161c)

## 🚀 **Funcionalidades**

✅ **Gerenciamento de Usuários:** Cadastro, edição e remoção de funcionários e clientes.  

![{2EED3C7E-6176-4BD0-8BDA-6A59BC28BC51}](https://github.com/user-attachments/assets/0b835f7d-96d8-4f0e-99fe-2088c90606ce)

![{4614854D-00FF-4EDC-BE07-F92D078D8E66}](https://github.com/user-attachments/assets/1739ae75-2204-47d6-a0df-d8a6387d0909)

✅ **Cadastro de Serviços:** Registra serviços disponíveis, com valores e percentuais de comissão.  

![{DC6AC2C3-16F6-4808-AF5F-264D8A050A12}](https://github.com/user-attachments/assets/ee64d5e6-d027-4af4-bebf-9e00a1a5eff5)

✅ **Registro de Serviços Realizados:** Associa um serviço a um cliente e prestador, com cálculo automático de comissão.  

![{58D91D12-31AF-4296-8159-5E04F0F2BEFD}](https://github.com/user-attachments/assets/4e73add5-ef0a-409f-b110-1d30ab12676a)

✅ **Relatórios Financeiros:** Geração de relatórios detalhados por período e prestador, exportáveis em **PDF** ou **Excel**.  

* Em desenvolvimento

✅ **Controle de Acesso:** Diferencia permissões entre administradores e usuários comuns.  

Usuarios comuns não tem acesso a emissão de relatórios, cadasto de usuários e cadastro de novo tipo de serviço. Essas funções ficam para os usuários de tipo admin.

---

## 🛠 **Tecnologias Utilizadas**

- **Linguagem:** Java 21 (Swing para UI)  
- **Banco de Dados:** MySQL  
- **Gerenciamento de Dependências:** Maven  
- **Relatórios:** JasperReports/iReport  
- **Bibliotecas Auxiliares:**  
  - `mysql-connector-j` → Conexão com MySQL  
  - `commons-dbutils` → Manipulação de banco de dados  
  - `rs2xml` → Exibição de tabelas no frontend  

---

## 📦 **Instalação**

1️⃣ **Clone o repositório:**  
```bash
git clone https://github.com/antonio-carlosjr/saloonProject
```

2️⃣ **Configure o banco de dados:**  
- Crie um banco de dados chamado `dbsalaom`.  
- Configure as credenciais no arquivo `ModuloConexao.java`.  

3️⃣ **Compile e execute o projeto:**  
```bash
mvn clean install
java -jar target/SaloonProject-1.0-SNAPSHOT.jar
```

---

## 🎮 **Capturas de Tela**
📷 *Adicione imagens do sistema aqui*  

---

## 📌 **Estrutura do Projeto**
```
📂 src/main/java/br/com/saloonproject
 ┣ 📂 dal                # Módulo de conexão com MySQL
 ┣ 📂 telas              # Interface gráfica e telas do sistema
 ┃ ┣ 📜 TelaLogin.java       # Tela de login
 ┃ ┣ 📜 TelaPrincipal.java   # Tela principal do sistema
 ┃ ┣ 📜 TelaCliente.java     # Gerenciamento de clientes
 ┃ ┣ 📜 TelaUsuario.java     # Controle de usuários
 ┃ ┣ 📜 TelaNovoServico.java # Cadastro de serviços
 ┃ ┣ 📜 TelaServicoRealizado.java  # Registro de serviços realizados
 ┃ ┣ 📜 TelaRelatorio.java   # Relatórios e exportação
 ┃ ┣ 📜 TelaSobre.java       # Informações sobre o sistema
``` 

---

## 📄 **Licença**
Este projeto é licenciado sob a **GPL (General Public License)**, permitindo que qualquer pessoa use e modifique o código livremente.  

---

## 📬 **Contato**
Caso tenha dúvidas ou sugestões, entre em contato:  
📧 **Email:** profissional.contatocarlos@gmail.com
📌 **GitHub:** [Antônio Carlos](https://github.com/antonio-carlosjr)  

