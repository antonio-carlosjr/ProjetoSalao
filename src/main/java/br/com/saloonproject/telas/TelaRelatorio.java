package br.com.saloonproject.telas;

import br.com.saloonproject.dal.ModuloConexao;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

public class TelaRelatorio extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public TelaRelatorio() {
        initComponents();
        conexao = ModuloConexao.conect();
        carregarPrestador();
        carregarServicos();
        txtDataInicial.setText(dataSistema);
        txtDataFinal.setText(dataSistema);

        // Configurar listeners para atualizar campos dinamicamente
        buttonRelatorioGeral.addActionListener(e -> atualizarCampos());
        buttonPorServico.addActionListener(e -> atualizarCampos());
        buttonRelatorioPorPrestador.addActionListener(e -> atualizarCampos());
        atualizarCampos();
    }

    LocalDateTime agora = LocalDateTime.now();
    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String dataSistema = agora.format(formatador);

    // Método para atualizar estado dos ComboBoxes
    private void atualizarCampos() {
        cbxServico.setEnabled(buttonPorServico.isSelected());
        cbxPrestador.setEnabled(buttonRelatorioPorPrestador.isSelected());

        if (!buttonPorServico.isSelected()) {
            cbxServico.setSelectedIndex(0);
        }
        if (!buttonRelatorioPorPrestador.isSelected()) {
            cbxPrestador.setSelectedIndex(0);
        }
    }

    // Validação robusta de datas e campos
    private boolean validarCampos() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // Impede datas inválidas (ex: 30/02)

        try {
            Date dataIni = sdf.parse(txtDataInicial.getText());
            Date dataFim = sdf.parse(txtDataFinal.getText());

            if (dataIni.after(dataFim)) {
                JOptionPane.showMessageDialog(null, "Data Inicial maior que a final!");
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Data inválida, use (dd/MM/yyyy)");
            return false;
        }

        // Validação dos ComboBoxes
        if (buttonPorServico.isSelected() && cbxServico.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Selecione um serviço!");
            return false;
        }
        if (buttonRelatorioPorPrestador.isSelected() && cbxPrestador.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Selecione um prestador!");
            return false;
        }
        return true;
    }

    // Geração do relatório com tratamento de erros
    private void gerarRelatorio(String formato) {
        try {
            // Caminho atualizado e sem acentos
            String caminhoRelatorio = "C:\\Users\\Carlos\\Documents\\NetBeansProjects\\SaloonProject\\reports\\Servico.jasper";
            File relatorio = new File(caminhoRelatorio);

            if (!relatorio.exists()) {
                JOptionPane.showMessageDialog(this, "Arquivo de modelo não encontrado! \n"
                        + "Verifique o caminho: \n" + caminhoRelatorio, "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
                   
            // Verifica a conexão com o banco de dados
            if (conexao == null) {
                JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Carrega o relatório já compilado
            JasperReport jasperReport = (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(relatorio);

            // Passagem de parâmetros
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("DATA_INICIAL", txtDataInicial.getText());
            parametros.put("DATA_FINAL", txtDataFinal.getText());

            // Gera o relatório com conexão ao banco de dados
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, conexao);

            // Define o local de saída do relatório gerado
            String nomeArquivo = "C:\\Users\\Carlos\\Documents\\NetBeansProjects\\SaloonProject\\reports\\relatorio_servicos_" + System.currentTimeMillis();

            // Exporta para PDF ou Excel
            if ("PDF".equalsIgnoreCase(formato)) {
                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(nomeArquivo + ".pdf"));
                exporter.exportReport();
                JOptionPane.showMessageDialog(this, "Relatório PDF gerado com sucesso!\nLocal: " + nomeArquivo + ".pdf");
            } else {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(nomeArquivo + ".xlsx"));
                exporter.exportReport();
                JOptionPane.showMessageDialog(this, "Relatório Excel gerado com sucesso!\nLocal: " + nomeArquivo + ".xlsx");
            }

        } catch (JRException e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro inesperado: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

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

    private void btGeraRelatorioActionPerformed() {
        if (validarCampos()) {
            if (buttonRelatorioGeral.isSelected()) {
                gerarRelatorio("PDF");
            } else {
                gerarRelatorio("Excel");
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoRelatorio = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        txtDataInicial = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDataFinal = new javax.swing.JTextField();
        buttonRelatorioGeral = new javax.swing.JRadioButton();
        buttonRelatorioPorPrestador = new javax.swing.JRadioButton();
        btGeraRelatorio = new javax.swing.JButton();
        buttonPorServico = new javax.swing.JRadioButton();
        cbxServico = new javax.swing.JComboBox<>();
        cbxPrestador = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Relatorio");

        jLabel2.setText("Data Inicial");

        txtDataInicial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataInicialActionPerformed(evt);
            }
        });

        jLabel3.setText("Data Final");

        txtDataFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataFinalActionPerformed(evt);
            }
        });

        tipoRelatorio.add(buttonRelatorioGeral);
        buttonRelatorioGeral.setText("Relatório Geral");
        buttonRelatorioGeral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRelatorioGeralActionPerformed(evt);
            }
        });

        tipoRelatorio.add(buttonRelatorioPorPrestador);
        buttonRelatorioPorPrestador.setText("Relatório por prestador");
        buttonRelatorioPorPrestador.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRelatorioPorPrestadorActionPerformed(evt);
            }
        });

        btGeraRelatorio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/addicon.png"))); // NOI18N
        btGeraRelatorio.setToolTipText("Adicionar");
        btGeraRelatorio.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btGeraRelatorio.setPreferredSize(new java.awt.Dimension(80, 80));
        btGeraRelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGeraRelatorioActionPerformed(evt);
            }
        });

        tipoRelatorio.add(buttonPorServico);
        buttonPorServico.setText("Relatório por serviço");
        buttonPorServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPorServicoActionPerformed(evt);
            }
        });

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

        jLabel6.setText("Servico");

        jLabel8.setText("Prestador");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonPorServico)
                            .addComponent(buttonRelatorioPorPrestador)
                            .addComponent(buttonRelatorioGeral))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxPrestador, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(cbxServico, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(88, 88, 88))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(56, 56, 56)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(258, 399, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(302, 302, 302)
                .addComponent(btGeraRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(124, 124, 124)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxPrestador, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonRelatorioGeral)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonPorServico)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRelatorioPorPrestador)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(btGeraRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(110, 110, 110))
        );

        setSize(new java.awt.Dimension(720, 630));
    }// </editor-fold>//GEN-END:initComponents

    private void txtDataInicialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataInicialActionPerformed

        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataInicialActionPerformed

    private void txtDataFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataFinalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataFinalActionPerformed

    private void buttonRelatorioGeralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRelatorioGeralActionPerformed

        // TODO add your handling code here:
    }//GEN-LAST:event_buttonRelatorioGeralActionPerformed

    private void buttonRelatorioPorPrestadorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRelatorioPorPrestadorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonRelatorioPorPrestadorActionPerformed

    private void btGeraRelatorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGeraRelatorioActionPerformed

        btGeraRelatorioActionPerformed();

    }//GEN-LAST:event_btGeraRelatorioActionPerformed

    private void buttonPorServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPorServicoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonPorServicoActionPerformed

    private void cbxServicoAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_cbxServicoAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxServicoAncestorAdded

    private void cbxServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxServicoActionPerformed

    }//GEN-LAST:event_cbxServicoActionPerformed

    private void cbxPrestadorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPrestadorActionPerformed

    }//GEN-LAST:event_cbxPrestadorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btGeraRelatorio;
    private javax.swing.JRadioButton buttonPorServico;
    private static javax.swing.JRadioButton buttonRelatorioGeral;
    private javax.swing.JRadioButton buttonRelatorioPorPrestador;
    private javax.swing.JComboBox<String> cbxPrestador;
    private javax.swing.JComboBox<String> cbxServico;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup tipoRelatorio;
    private javax.swing.JTextField txtDataFinal;
    private javax.swing.JTextField txtDataInicial;
    // End of variables declaration//GEN-END:variables

}
