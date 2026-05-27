package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ArduinoInput {

    private SerialPort porta;
    private BufferedReader reader;
    private volatile boolean botaoPressionado = false;
    private Thread threadLeitura;

    // Tente "COM3", "COM4"... no Windows, ou "/dev/ttyUSB0" no Linux/Mac
    private static final String NOME_PORTA = "COM3";
    private static final int BAUD_RATE = 115200;

    public void conectar() {
        porta = SerialPort.getCommPort(NOME_PORTA);
        porta.setBaudRate(BAUD_RATE);

        if (porta.openPort()) {
            System.out.println("Arduino conectado em " + NOME_PORTA);
            reader = new BufferedReader(new InputStreamReader(porta.getInputStream()));
            iniciarLeitura();
        } else {
            System.out.println("Falha ao conectar. Verifique a porta: " + NOME_PORTA);
        }
    }

    private void iniciarLeitura() {
        threadLeitura = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String linha = reader.readLine();
                    if (linha != null && linha.trim().equals("BOTAO")) {
                        botaoPressionado = true; // sinaliza para o jogo
                    }
                }
            } catch (Exception e) {
                System.out.println("Leitura serial encerrada.");
            }
        });
        threadLeitura.setDaemon(true); // encerra junto com o jogo
        threadLeitura.start();
    }

    /** Chame isso no logic() — retorna true UMA vez por pressionamento */
    public boolean consumirBotao() {
        if (botaoPressionado) {
            botaoPressionado = false;
            return true;
        }
        return false;
    }

    public void desconectar() {
        if (threadLeitura != null) threadLeitura.interrupt();
        if (porta != null && porta.isOpen()) porta.closePort();
    }
}