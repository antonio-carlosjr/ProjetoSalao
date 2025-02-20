package br.com.saloonproject.telas;

import br.com.saloonproject.dal.ModuloConexao;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class TelaServicoRealizado extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public TelaServicoRealizado() {
        initComponents();
        conexao = ModuloConexao.conect();
        carregarServicos();
        carregarPrestador();
        txtDataServico.setText(dataSistema);

    }

    LocalDateTime agora = LocalDateTime.now();
    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String dataSistema = agora.format(formatador);

    private void carregarServicos() {
        String sql = "SELECT nmservico FROM tbservico";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cbxServico.addItem(rs.getString("nmservico"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar serviços: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    private void carregarPrestador() {
        String sql = "SELECT usuario FROM tbusuarios";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cbxPrestador.addItem(rs.getString("usuario"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar usuarios: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    private void pesquisar_clientes() {
        String sql = "select idcli as ID, nomecli as Nome, fone as Telefone from tbclientes where nomecli like ?";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtBuscaCli.getText() + "%");
            rs = pst.executeQuery();
            tblClientes.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void setar_campos() {
        int setar = tblClientes.getSelectedRow();
        CliId.setText(tblClientes.getModel().getValueAt(setar, 0).toString());

    }

    //metodo para cadastrar servico realizado
    private void cadastrar_servicoRealizado() {
        String sql = "INSERT INTO tbservico_realizado(tipo_pagamento, matservico, valor, iduser, idcli, valor_comissao) VALUES (?, ?, ?, ?, ?, ?)";

        try {

            // Buscar IDs do serviço e do prestador
            int idServico = buscarId("tbservico", "nmservico", cbxServico.getSelectedItem().toString(), "matservico");
            int idPrestador = buscarId("tbusuarios", "usuario", cbxPrestador.getSelectedItem().toString(), "iduser");

            // Validar IDs retornados            
            if (idServico == -1) {
                JOptionPane.showMessageDialog(null, "Serviço selecionado não encontrado no banco de dados.");
                return;
            }
            if (idPrestador == -1) {
                JOptionPane.showMessageDialog(null, "Prestador selecionado não encontrado no banco de dados.");
                return;
            }

            double valorTotal = Double.parseDouble(txtValor.getText());
            System.out.println("Valor total: " + valorTotal);
            double percentualComissao = buscarComissao(cbxServico.getSelectedItem().toString());
            System.out.println("Percentual de comissao " + percentualComissao);

            double valorFinalComissao = valorTotal * (percentualComissao / 100);
            System.out.println("Final: " + valorFinalComissao);

            pst = conexao.prepareStatement(sql);

            // Preencher os parâmetros
            pst.setString(1, cbxTipoPagamentoServico.getSelectedItem().toString());
            pst.setInt(2, idServico);
            pst.setString(3, txtValor.getText());
            pst.setInt(4, idPrestador);
            pst.setInt(5, Integer.parseInt(CliId.getText()));
            pst.setDouble(6, valorFinalComissao);
            // Executar inserção
            int adicionado = pst.executeUpdate();
            if (adicionado > 0) {
                JOptionPane.showMessageDialog(null, "Serviço realizado cadastrado com sucesso!");
                CliId.setText(null);
                txtValor.setText(null);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar serviço: " + e.getMessage());
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar PreparedStatement: " + e.getMessage());
            }
        }
    }

    private int buscarComissao(String nomeServico) {
        String sql = "SELECT compercent FROM tbservico WHERE nmservico = ?";
        int comissao = -1; // Valor padrão caso o ID não seja encontrado

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, nomeServico);
            rs = pst.executeQuery();

            if (rs.next()) {
                comissao = rs.getInt("compercent"); // Captura o percentual
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar o percentual: " + e.getMessage());
        } finally {
            // Fechar ResultSet e PreparedStatement para evitar vazamentos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
            }
        }

        return comissao;
    }

    private int buscarId(String tabela, String colunaNome, String nome, String colunaID) {
        String sql = "SELECT " + colunaID + " FROM " + tabela + " WHERE " + colunaNome + " = ?";
        int id = -1; // Valor padrão caso o ID não seja encontrado

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, nome);
            rs = pst.executeQuery();

            if (rs.next()) {
                id = rs.getInt(colunaID); // Captura o ID
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar ID: " + e.getMessage());
        } finally {
            // Fechar ResultSet e PreparedStatement para evitar vazamentos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
            }
        }

        return id;
    }

    private void adicionarServico() {
        String sql = "INSERT INTO tbservico_realizado (idcli, matservico_realizado, iduser, valor, data_servico) VALUES (?, ?, ?, ?, ?)";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, CliId.getText());
            pst.setString(2, cbxServico.getSelectedItem().toString());
            pst.setString(3, cbxPrestador.getSelectedItem().toString());
            pst.setString(4, txtValor.getText());
            pst.setString(5, txtDataServico.getText());

            if (CliId.getText().isEmpty() || txtValor.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios");
            } else {
                int adicionado = pst.executeUpdate();
                if (adicionado > 0) {
                    JOptionPane.showMessageDialog(null, "Serviço adicionado com sucesso");
                    limparCampos();

                }
            }
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void btAddServico(java.awt.event.ActionEvent evt) {
        adicionarServico();
    }

    private void removerServico() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este serviço?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM tbservicosrealizados WHERE idservico=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtNumServico.getText()); // Considerando que jTextField2 armazena o ID do serviço
                int apagado = pst.executeUpdate();
                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Serviço removido com sucesso");
                    limparCampos();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    private void limparCampos() {
        txtDataServico.setText(null); // Data
        txtNumServico.setText(null); // ID do Serviço
        cbxServico.setSelectedIndex(0);
        cbxPrestador.setSelectedIndex(0);
        txtValor.setText(null);
        CliId.setText(null);
    }

    private void btDelServico(java.awt.event.ActionEvent evt) {
        removerServico();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtDataServico = new javax.swing.JTextField();
        txtNumServico = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cbxTipoPagamentoServico = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        txtBuscaCli = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        CliId = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        txtValor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cbxServico = new javax.swing.JComboBox<>();
        cbxPrestador = new javax.swing.JComboBox<>();
        btAddServico = new javax.swing.JButton();
        btDelServico = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Serviço");
        setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("N° Servico");

        jLabel2.setText("Data");

        txtDataServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataServicoActionPerformed(evt);
            }
        });

        txtNumServico.setEnabled(false);
        txtNumServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumServicoActionPerformed(evt);
            }
        });

        jLabel3.setText("Método de Pagamento");

        cbxTipoPagamentoServico.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Dinheiro", "Cartão", "Pix" }));
        cbxTipoPagamentoServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxTipoPagamentoServicoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxTipoPagamentoServico, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txtNumServico, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtDataServico, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDataServico, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumServico, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(53, 53, 53)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxTipoPagamentoServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));

        txtBuscaCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscaCliActionPerformed(evt);
            }
        });
        txtBuscaCli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscaCliKeyReleased(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/searchIcon.png"))); // NOI18N

        CliId.setEnabled(false);
        CliId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CliIdActionPerformed(evt);
            }
        });

        jLabel5.setText("*ID");

        tblClientes = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Nome", "Telefone"
            }
        ));
        tblClientes.setFocusable(false);
        tblClientes.getTableHeader().setReorderingAllowed(false);
        tblClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClientes);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtBuscaCli)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CliId, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(CliId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addComponent(txtBuscaCli, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jLabel6.setText("Servico");

        jLabel8.setText("Prestador");

        jLabel9.setText("Valor");

        cbxServico.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxServico.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                cbxServicoAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        cbxServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxServicoActionPerformed(evt);
            }
        });

        cbxPrestador.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxPrestador.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxPrestadorActionPerformed(evt);
            }
        });

        btAddServico.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/addicon.png"))); // NOI18N
        btAddServico.setToolTipText("Adicionar");
        btAddServico.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btAddServico.setPreferredSize(new java.awt.Dimension(80, 80));
        btAddServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddServicoActionPerformed(evt);
            }
        });

        btDelServico.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/deleticon.png"))); // NOI18N
        btDelServico.setToolTipText("Excluir");
        btDelServico.setMaximumSize(new java.awt.Dimension(54, 54));
        btDelServico.setMinimumSize(new java.awt.Dimension(54, 54));
        btDelServico.setPreferredSize(new java.awt.Dimension(80, 80));
        btDelServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDelServicoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(149, 149, 149)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxPrestador, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbxServico, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(35, 35, 35)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(244, 244, 244)
                                .addComponent(btAddServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(btDelServico, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxPrestador, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))))
                .addGap(79, 79, 79)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btAddServico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btDelServico, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(101, Short.MAX_VALUE))
        );

        setBounds(0, 0, 720, 630);
    }// </editor-fold>//GEN-END:initComponents

    private void txtNumServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumServicoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNumServicoActionPerformed

    private void tblClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesMouseClicked
        setar_campos();
    }//GEN-LAST:event_tblClientesMouseClicked

    private void cbxServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxServicoActionPerformed
    }//GEN-LAST:event_cbxServicoActionPerformed

    private void cbxPrestadorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPrestadorActionPerformed
    }//GEN-LAST:event_cbxPrestadorActionPerformed

    private void btAddServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddServicoActionPerformed
        cadastrar_servicoRealizado();
    }//GEN-LAST:event_btAddServicoActionPerformed

    private void btDelServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDelServicoActionPerformed
    }//GEN-LAST:event_btDelServicoActionPerformed


    private void txtBuscaCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscaCliActionPerformed
    }//GEN-LAST:event_txtBuscaCliActionPerformed

    private void txtBuscaCliKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscaCliKeyReleased
        pesquisar_clientes();
    }//GEN-LAST:event_txtBuscaCliKeyReleased

    private void cbxTipoPagamentoServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTipoPagamentoServicoActionPerformed
        // Atribuindo um texto a variavel tipo se selecionado

    }//GEN-LAST:event_cbxTipoPagamentoServicoActionPerformed

    private void CliIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CliIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CliIdActionPerformed

    private void cbxServicoAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_cbxServicoAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxServicoAncestorAdded

    private void txtDataServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataServicoActionPerformed

        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataServicoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CliId;
    private javax.swing.JButton btAddServico;
    private javax.swing.JButton btDelServico;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbxPrestador;
    private javax.swing.JComboBox<String> cbxServico;
    private javax.swing.JComboBox<String> cbxTipoPagamentoServico;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtBuscaCli;
    private javax.swing.JTextField txtDataServico;
    private javax.swing.JTextField txtNumServico;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
