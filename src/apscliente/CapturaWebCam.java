/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apscliente;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JFrame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import Mensagem.Mensagem;
import javax.swing.JOptionPane;

/**
 *
 * @author Gabriel Borges
 */
public class CapturaWebCam extends javax.swing.JFrame {

    /**
     * Creates new form CapturaWebCam
     */
    private int id;
    private Mat faceCapturada = null;
    private int fotosCapturadas = 0;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private JFrame retorno;
    private JFrame inicial;
    private Socket conexao;
    private boolean statusEnviando = false;
    private Mensagem retornoCerto = null;
    private String ip;
    private String tipo;
    private OpenCVFrameGrabber camera;
    public CapturaWebCam() {
        initComponents();
    }

    public JFrame getInicial() {
        return inicial;
    }

    public void setInicial(JFrame inicial) {
        this.inicial = inicial;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStatusEnviando() {
        return statusEnviando;
    }

    public void setStatusEnviando(boolean statusEnviando) {
        this.statusEnviando = statusEnviando;
    }

    public void iniciar(String tipo, ObjectOutputStream saida, ObjectInputStream entrada, Socket conexao, JFrame retorno) {
        this.saida = saida;
        this.retorno = retorno;
        this.tipo = tipo;
        new Thread(() -> {
            fotosCapturadas = 1;
            int limite = 25;
            try {
                if (tipo == "Cadastro") {
                    JOptionPane.showMessageDialog(this, "Atenção, só tire a foto quando o algoritmo de reconhecimento reconhecer seu rosto,"
                            + "\r\nAparecerá um retangulo no seu rosto quando isso acontecer.\r\nDicas para melhorar o Reconhecimento facial:\r\n"
                            + "Esteja em um ambiente bem iluminado.\r\n"
                            + "Teste adicionar variações de expressão(Feliz, triste, com e sem óculos e etc...)\r\n"
                            + "Alterne os Angulos das fotos(Olhando levemente para cima, baixo, direita e esquerda)\r\n");
                    FaceRecognizer r = EigenFaceRecognizer.create();
                    OpenCVFrameConverter.ToMat converteMat = new OpenCVFrameConverter.ToMat();
                    camera = new OpenCVFrameGrabber(0);
                    camera.start();
                    Frame frameCapturado = null;
                    Java2DFrameConverter conversor = new Java2DFrameConverter();
                    CascadeClassifier detector = new CascadeClassifier("src\\Resources\\haarcascade_frontalface_alt.xml");
                    Mat imagemColorida = new Mat();
                    waitWebCam.setText("0/25");
                    while ((frameCapturado = camera.grab()) != null && fotosCapturadas <= limite) {
                        imagemColorida = converteMat.convert(frameCapturado);
                        Mat imagemCinza = new Mat();
                        cvtColor(imagemColorida, imagemCinza, COLOR_BGRA2GRAY);
                        RectVector facesDetectadas = new RectVector();
                        detector.detectMultiScale(imagemCinza, facesDetectadas, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));
                        for (int i = 0; i < facesDetectadas.size(); i++) {
                            Rect dadosFace = facesDetectadas.get(0);
                            rectangle(imagemColorida, dadosFace, new Scalar(0, 0, 255, 0));
                            faceCapturada = new Mat(imagemCinza, dadosFace);
                            Size tamanho = new Size(160, 160);
                            opencv_imgproc.resize(faceCapturada, faceCapturada, tamanho);
                        }
                        BufferedImage imagem = conversor.getBufferedImage(frameCapturado);
                        Graphics g = cameraView.getGraphics();
                        g.drawImage(imagem, 10, 10, cameraView.getWidth(), cameraView.getHeight(), cameraView);
                    }
                    JOptionPane.showMessageDialog(this, "Fotos armazenadas com sucesso\r\nTalvez seja preciso aguardar alguns instantes antes de realizar a Autenticação Biométrica.");
                    saida.writeObject(new Mensagem("FinalizoCadsatroImagem"));
                    retorno.setVisible(true);
                    this.dispose();
                    camera.stop();
                } else {
                    enviarFoto.setVisible(false);
                    FaceRecognizer r = EigenFaceRecognizer.create();
                    OpenCVFrameConverter.ToMat converteMat = new OpenCVFrameConverter.ToMat();
                    camera = new OpenCVFrameGrabber(0);
                    camera.start();
                    Frame frameCapturado = null;
                    Java2DFrameConverter conversor = new Java2DFrameConverter();
                    CascadeClassifier detector = new CascadeClassifier("src\\Resources\\haarcascade_frontalface_alt.xml");
                    Mat imagemColorida = new Mat();
                    waitWebCam.setText("");
                    while ((frameCapturado = camera.grab()) != null && retornoCerto == null) {
                        imagemColorida = converteMat.convert(frameCapturado);
                        Mat imagemCinza = new Mat();
                        cvtColor(imagemColorida, imagemCinza, COLOR_BGRA2GRAY);
                        RectVector facesDetectadas = new RectVector();
                        detector.detectMultiScale(imagemCinza, facesDetectadas, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));
                        for (int i = 0; i < facesDetectadas.size(); i++) {
                            Rect dadosFace = facesDetectadas.get(0);
                            rectangle(imagemColorida, dadosFace, new Scalar(0, 0, 255, 0));
                            faceCapturada = new Mat(imagemCinza, dadosFace);
                            Size tamanho = new Size(160, 160);
                            opencv_imgproc.resize(faceCapturada, faceCapturada, tamanho);
                            //System.out.println(statusEnviando);
                            if (!statusEnviando) {
                                statusEnviando = true;
                                enviaFoto t = new enviaFoto();
                                t.setConexao(conexao);
                                t.setEntrada(entrada);
                                t.setClasseMae(this);
                                t.start();
                            }
                        }
                        BufferedImage imagem = conversor.getBufferedImage(frameCapturado);
                        Graphics g = cameraView.getGraphics();
                        g.drawImage(imagem, 10, 10, cameraView.getWidth(), cameraView.getHeight(), cameraView);
                    }
                    JOptionPane.showMessageDialog(this, "Usuario: " + retornoCerto.getNomeUsuario() + " Logado com Sucesso");
                    camera.stop();
                    this.dispose();
                    Chat chat = new Chat();
                    chat.setUsuario(retornoCerto.getNomeUsuario());
                    chat.setCargo(retornoCerto.getCargo());
                    chat.setSaida(saida);
                    chat.setIpServer(ip);
                    chat.setCon(conexao);
                    chat.startListen(entrada);
                    chat.setTelaAnterior(this);
                    chat.setVisible(true);
                    chat.setTelaAnterior(inicial);
                }
            } catch (FrameGrabber.Exception ex) {
                Logger.getLogger(APSCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CapturaWebCam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cameraView = new javax.swing.JPanel();
        waitWebCam = new javax.swing.JLabel();
        enviarFoto = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 153));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 0, 152));

        cameraView.setBackground(new java.awt.Color(0, 0, 152));

        javax.swing.GroupLayout cameraViewLayout = new javax.swing.GroupLayout(cameraView);
        cameraView.setLayout(cameraViewLayout);
        cameraViewLayout.setHorizontalGroup(
            cameraViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 553, Short.MAX_VALUE)
        );
        cameraViewLayout.setVerticalGroup(
            cameraViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );

        waitWebCam.setHorizontalAlignment(SwingConstants.CENTER);
        waitWebCam.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        waitWebCam.setForeground(new java.awt.Color(255, 255, 255));
        waitWebCam.setText("CARREGANDO WEBCAM... AGUARDE!");

        enviarFoto.setText("Tirar Foto");
        enviarFoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarFotoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(waitWebCam, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cameraView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(239, 239, 239)
                        .addComponent(enviarFoto)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(cameraView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(waitWebCam)
                .addGap(1, 1, 1)
                .addComponent(enviarFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void enviarFotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarFotoActionPerformed
        int cols = faceCapturada.cols();
        int rows = faceCapturada.rows();
        int elemSize = (int) faceCapturada.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        faceCapturada.data().get(data);
        if (this.faceCapturada != null) {
            try {
                waitWebCam.setText(fotosCapturadas + "/25");
                saida.writeObject(new Mensagem("EnvioImagemCadastro", this.id, data, this.fotosCapturadas, rows, cols, faceCapturada.type()));
                this.fotosCapturadas++;
            } catch (IOException ex) {
                Logger.getLogger(CapturaWebCam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_enviarFotoActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (tipo == "Cadastro" && fotosCapturadas <= 25) {
            JOptionPane.showMessageDialog(this, "Por favor, finalize a captura das fotos antes de sair.");
        } else {
            try {
                camera.stop();
                saida.writeObject(new Mensagem("SairDoServidor"));
                saida.close();
            } catch (IOException ex) {
                Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
            }

            retorno.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CapturaWebCam.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CapturaWebCam.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CapturaWebCam.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CapturaWebCam.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CapturaWebCam().setVisible(true);
            }
        });
    }

    public class enviaFoto extends Thread {

        private Socket conexao;
        private ObjectInputStream entrada;
        private CapturaWebCam classeMae;

        public CapturaWebCam getClasseMae() {
            return classeMae;
        }

        public void setClasseMae(CapturaWebCam classeMae) {
            this.classeMae = classeMae;
        }

        public Socket getConexao() {
            return conexao;
        }

        public ObjectInputStream getEntrada() {
            return entrada;
        }

        public void setEntrada(ObjectInputStream entrada) {
            this.entrada = entrada;
        }

        public void setConexao(Socket conexao) {
            this.conexao = conexao;
        }

        public void run() {

            try {
                int cols = faceCapturada.cols();
                int rows = faceCapturada.rows();
                int elemSize = (int) faceCapturada.elemSize();
                byte[] data = new byte[cols * rows * elemSize];
                faceCapturada.data().get(data);
                saida.writeObject(new Mensagem("TentativaLoginImagem", 0, data, 0, rows, cols, faceCapturada.type()));
                while (true) {
                    //System.out.println(conexao);
                    if (conexao.getInputStream().available() > 0) {
                        Mensagem retorno = (Mensagem) entrada.readObject();
                        //System.out.println(retorno.getMsg());
                        if (!retorno.getTipo().equals("Erro")) {
                            retornoCerto = retorno;
                            break;
                        } else {
                            classeMae.setStatusEnviando(false);
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CapturaWebCam.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CapturaWebCam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cameraView;
    private javax.swing.JButton enviarFoto;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel waitWebCam;
    // End of variables declaration//GEN-END:variables
}
