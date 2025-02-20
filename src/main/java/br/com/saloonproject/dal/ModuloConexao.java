package br.com.saloonproject.dal;

import java.sql.*;

//@author Antônio Carlos da Silva Júnior
public class ModuloConexao {

    //metodo que estabelece a conexao com o BD
    public static Connection conect() {

        java.sql.Connection conexao = null;
        // A linha seguinte "chama o driver"
        String driver = "com.mysql.cj.jdbc.Driver";
        //Informações do BD
        String url = "jdbc:mysql://127.0.0.1:3306/dbsalaom?characterEncoding=utf-8";
        String user = "dba";
        String password = "123123*A";
        //Estabelecendo conexao
        try {
            Class.forName(driver);
            conexao = DriverManager.getConnection(url, user, password);
            return conexao;

        } catch (Exception e) {
            //linha para detectar os erros de conexao
            //System.out.println(e);
            return null;
        }
    }

}
