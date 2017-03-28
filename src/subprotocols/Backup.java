package subprotocols;
import java.io.File;

/**
 * Created by Francisca on 28/03/17.
 */
public class Backup extends Thread {
    private File file;
    private int replicationDeg;

    public Backup(String fileName, int replicationDeg) {
        this.file = new File("../res/"+fileName);
        this.replicationDeg = replicationDeg;
    }

    public void run() {
        //TENTAR LER O FICHEIRO SE POSITIVO ENVIAR CHUNKS
    }

    public void sendChunks(){
        //GUARDAR DADOS FICHEIRO
        //METER TAMANHO MAXIMO DO CHUNK
        //CRIAR O HEADER

        //CICLO ENVIAR CHUNKS ATÉ ACABAR TAMANHO DO FICHEIRO (CRIAR FUNÇÃO DE ENVIAR O CHUNK)

        //VERIFICAR SE PEER GUARDOU FICHEIRO COMO BACKUP
    }

}
