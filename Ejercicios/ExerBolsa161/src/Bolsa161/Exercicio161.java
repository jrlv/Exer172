
package Bolsa161;

/**
 *
 * @author JRLV
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

interface Bolsa{
    boolean iniciar();
    boolean actualizar();
    boolean novo(String login, String clave, float capital);
    boolean identificar(String login, String clave);
}

interface Inversor{
    boolean comprar(int idEmpresa, int cantidade);
    boolean vender(int idEmpresa, int cantidade);
    float valorar();
}

interface Resumir{
    String resumir();
}


class BolsaEnBD implements Bolsa, Resumir{
    
    private int idEmpresa;
    private String nome;
    private float valor;
    private Connection con;

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
        
    
    public boolean iniciar(){
        boolean iniciado = false;
        try{    
            con = DriverManager.getConnection("jdbc:mysql://localhost:3308/", "root", "1234");
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }        
        Statement st;
        try{
            st = con.createStatement();
            st.execute("CREATE DATABASE IF NOT EXISTS bolsa");
            st.execute("USE bolsa");
            st.execute("CREATE TABLE IF NOT EXISTS accions(idEmpresa int, nome varchar(50), valor float)");
            st.execute("CREATE TABLE IF NOT EXISTS clientes(login varchar(50), clave varchar(50), capital float)");
            st.execute("CREATE TABLE IF NOT EXISTS carteira(idEmpresa int, cliente varchar(50), cantidade int)");
            iniciado = true;
            
            System.out.println("Base de datos conectada.");
            
        } catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return iniciado;
    }
    
    public boolean conectar(){
        boolean conectar = false;
        try{
            con = DriverManager.getConnection("jdbc:mysql://localhost:3308/bolsa", "root", "1234");
            conectar = true;
            System.out.println("Base de datos conectada.");
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return conectar;
    }
    
    public boolean inserirAccions(int idEmpresa, String nome, float valor){
        boolean inserida = false;
        PreparedStatement pst;
        try{
            pst = con.prepareStatement("SELECT * FROM accions WHERE idEmpresa = ?");
            pst.setInt(1, idEmpresa);
            ResultSet rsIn = pst.executeQuery();
            if(rsIn.next()){
                System.out.println("As accións de " + nome + " xa están dadas de alta.");
            }else{
                pst = con.prepareStatement("INSERT INTO accions VALUES(?, ?, ?)");
                pst.setInt(1, idEmpresa);
                pst.setString(2, nome);
                pst.setFloat(3, valor);
                int inseridas = pst.executeUpdate();
                if(inseridas > 0){
                    inserida = true;
                    System.out.println("Acción de " + nome + " inserida correctamente.");
                }    
            }    
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return inserida;
    }
    
    
    public boolean novo(String login, String clave, float capital){
        boolean creado = false;
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT login FROM clientes WHERE login = ?");
            ps.setString(1, login);
            ResultSet r = ps.executeQuery();
            if(r.next()){
                System.out.println("Escolla outro nome, " + login + " xa existe.");
            } else{
                try{
                    ps = con.prepareStatement("INSERT INTO clientes VALUES(?, ?, ?)");
                    ps.setString(1, login);
                    ps.setString(2, clave);
                    ps.setFloat(3, capital);
                    int inseridos = ps.executeUpdate();
                    if (inseridos > 0){
                        creado = true;
                        System.out.println("Cliente " + login + " inserido.");
                    }
                }catch(SQLException e){
                    System.out.println(e.getLocalizedMessage());
                }   
            }  
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return creado;
    }
    
    public boolean identificar(String login, String clave){
        boolean identificado = false;
        PreparedStatement ps;
        String clienteActual = null;
        try{
            ps = con.prepareStatement("SELECT * FROM clientes WHERE login = ? AND clave = ?");
            ps.setString(1, login);
            ps.setString(2, clave);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                identificado = true;
                clienteActual = rs.getString("login");
                System.out.println("Cliente identificado: " + clienteActual);
            }  
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return identificado;
    }
    
    public boolean actualizar(){
        boolean actualizado = false;
        PreparedStatement p;
        try{
            p = con.prepareStatement("SELECT * FROM accions");
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                int id = rs.getInt(1);
                float valorActual = rs.getFloat(3);
                float variacionValor = (((float)(Math.random()*5)+1) - ((float)(Math.random()*5)+1));
                try{
                    p = con.prepareStatement("UPDATE accions SET valor = ? WHERE idEmpresa = ?");
                    p.setFloat(1, valorActual + variacionValor);
                    p.setInt(2, id);
                    p.executeUpdate();
                }catch(SQLException e){
                    System.out.println(e.getLocalizedMessage());
                }
            }
            rs.close();
            System.out.println("Actualización executada.");
            
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return actualizado;
    }
    
    public String resumir(){
        float valorBolsa = 0;
        PreparedStatement pr;
        try{
            pr = con.prepareStatement("SELECT SUM(valor) AS valorBolsa FROM accions");
            ResultSet rr = pr.executeQuery();
            
            while(rr.next()){
                valorBolsa = rr.getFloat("valorBolsa");
            }
            rr.close();
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        
        float cartos = 0;
        try{
            pr = con.prepareStatement("SELECT SUM(capital) AS cartos FROM clientes");
            ResultSet ri = pr.executeQuery();
            while(ri.next()){
                cartos = ri.getFloat("cartos");
            }
            ri.close();
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        
        int inversores = 0;
        try{
            pr = con.prepareStatement("SELECT COUNT(login) AS clientes FROM clientes");
            ResultSet rc = pr.executeQuery();
            while(rc.next()){
                inversores = rc.getInt("clientes");
            }
            rc.close();
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        System.out.println("Os inversores dispoñen actualmente de " + cartos + " euros para inversións.");
        System.out.println("O valor actual da Bolsa é de: " + valorBolsa + " euros.");
        System.out.println("Neste momento temos " + inversores + " clientes na base de datos da bolsa.");
        return "O valor actual da Bolsa é de: " + valorBolsa + " euros." + "\n"
                + "Os inversores dispoñen actualmente de " + cartos + " euros para inversións." + "\n"
                + "Neste momento temos " + inversores + " clientes na base de datos da bolsa.";
    }
}

class Cliente implements Inversor, Resumir{
    
    private BolsaEnBD b;
    private String clave;
    private String login;
    private float capital;

    public BolsaEnBD getB() {
        return b;
    }

    public void setB(BolsaEnBD b) {
        this.b = b;
    }
    
        public float getCapital() {
            return capital;
        }

        public void setCapital(float capital) {
            this.capital = capital;
        }

        public String getClave() {
            return clave;
        }

        public void setClave(String clave) {
            this.clave = clave;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    
    
    public boolean comprar(int idEmpresa, int cantidade){
        boolean comprou = false;
        PreparedStatement p;
        try{
            p = b.getCon().prepareStatement("SELECT valor FROM accions WHERE idEmpresa = ?");
            p.setFloat(1, idEmpresa);
            ResultSet rv = p.executeQuery();
            float valorDaAcción = 0;
            float costeCompra = 0;
            if(rv.next()){
                valorDaAcción = rv.getFloat("valor");
                costeCompra = valorDaAcción * cantidade;
                p = b.getCon().prepareStatement("SELECT capital FROM clientes WHERE login = ?");
                p.setString(1, this.getLogin());
                ResultSet rc = p.executeQuery();
                float capitalActual = 0;
                if(rc.next()){
                    capitalActual = rc.getFloat("capital");
                    if(capitalActual >= costeCompra){
                        p = b.getCon().prepareStatement("UPDATE clientes SET capital = ? WHERE login = ?");
                        p.setFloat(1, capitalActual - costeCompra);
                        p.setString(2, this.getLogin());
                        p.executeUpdate();
                        p = b.getCon().prepareStatement("SELECT cantidade FROM carteira WHERE cliente = ? AND idEmpresa = ?");
                        p.setString(1, this.getLogin());
                        p.setInt(2, idEmpresa);
                        ResultSet rs = p.executeQuery();
                        int cantidadeActual = 0;
                        if(rs.next()){
                            cantidadeActual = rs.getInt("cantidade");
                            p = b.getCon().prepareStatement("UPDATE carteira SET cantidade = ? WHERE cliente = ? AND idEmpresa = ?");
                            p.setInt(1, cantidadeActual + cantidade);
                            p.setString(2, this.getLogin());
                            p.setInt(3, idEmpresa);
                            p.executeUpdate();
                            comprou = true;
                            System.out.println("Compra de accións realizada correctamente.");
                        }else {
                            p = b.getCon().prepareStatement("INSERT INTO carteira VALUES(?,?,?)");
                            p.setInt(1, idEmpresa);
                            p.setString(2, this.getLogin());
                            p.setInt(3, cantidade);
                            p.executeUpdate();
                            comprou = true;
                            System.out.println("Compra de accións realizada correctamente.");
                        }
                    }else {
                        System.out.println(this.getLogin() + " non ten cartos para esta compra.");
                    }
                }else {
                    System.out.println("Erro no nome do cliente.");
                }
            }else {
                System.out.println("Esta empresa non existe na base de datos.");
            }
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return comprou;
    }
    
    public boolean vender(int idEmpresa, int cantidade){
        boolean vendido = false;
        PreparedStatement p;
        try{
            p = b.getCon().prepareStatement("SELECT valor FROM accions WHERE idEmpresa = ?");
            p.setFloat(1, idEmpresa);
            ResultSet rv = p.executeQuery();
            float valorDaAcción = 0;
            float valorVenda = 0;
            if(rv.next()){
                valorDaAcción = rv.getFloat("valor");
                valorVenda = valorDaAcción * cantidade;
                p = b.getCon().prepareStatement("SELECT cantidade FROM carteira WHERE cliente = ? AND idEmpresa = ?");
                p.setString(1, this.getLogin());
                p.setInt(2, idEmpresa);
                ResultSet rs = p.executeQuery();
                if(rs.next()){
                    int cantidadeActual = rs.getInt("cantidade");
                    if(cantidadeActual >= cantidade){
                        p = b.getCon().prepareStatement("UPDATE carteira SET cantidade = ? WHERE cliente = ? AND idEmpresa = ?");
                        p.setInt(1, cantidadeActual - cantidade);
                        p.setString(2, this.getLogin());
                        p.setInt(3, idEmpresa);
                        p.executeUpdate();
                        p = b.getCon().prepareStatement("SELECT capital FROM clientes WHERE login = ?");
                        p.setString(1, this.getLogin());
                        ResultSet rc = p.executeQuery();
                        float capitalActual = 0;
                        while(rc.next()){
                            capitalActual = rc.getFloat("capital");
                            p = b.getCon().prepareStatement("UPDATE clientes SET capital = ? WHERE login = ?");
                            p.setFloat(1, capitalActual + valorVenda);
                            p.setString(2, this.getLogin());
                            p.executeUpdate();
                        }
                        rc.close();
                        vendido = true;
                        System.out.println("Venta de accións realizada correctamente.");
                    }else {
                        System.out.println(this.getLogin() + " non ten tantas accións de esta empresa.");
                    }
                } else  {
                    System.out.println(this.getLogin() + " non ten accións de esta empresa.");
                }
            }else {
                System.out.println("Esta empresa non existe na base de datos.");
            }
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        return vendido;
    }
    
    public float valorar(){
        float valor = 0;
        try{
            PreparedStatement pv = b.getCon().prepareStatement("SELECT SUM(cantidade*valor) AS valoracion FROM carteira INNER JOIN accions ON carteira.idEmpresa = accions.idEmpresa WHERE carteira.cliente = ?");
            pv.setString(1, this.getLogin());
            ResultSet rsm = pv.executeQuery();
            while(rsm.next()){
                valor = rsm.getFloat("valoracion");    
                System.out.println("Valor da carteira de accións de " + this.getLogin() + ":" + valor);
            }
            rsm.close();
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
    return valor;
    }
    
    public String resumir(){
        float capitalTotal = 0;
        try{
            PreparedStatement pr = b.getCon().prepareStatement("SELECT capital FROM clientes WHERE login = ?");
            pr.setString(1, this.getLogin());
            ResultSet rt = pr.executeQuery();
            while(rt.next()){
                capitalTotal = this.valorar() + rt.getFloat("capital");
            }
            rt.close();
        }catch(SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
        System.out.println(this.getLogin() + " ten " + capitalTotal + " euros, sumando o capital e os cartos invertidos.");
        return this.getLogin() + " ten " + capitalTotal + " euros, sumando o capital e os cartos invertidos.";
    }
}

