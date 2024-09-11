 package eventoop;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import java.math.BigDecimal;

public class Barraupdate implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent arg0) throws Exception {
   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent event) throws Exception {
      DynamicVO OldVo = (DynamicVO)event.getOldVO();
      if (OldVo.asBigDecimal("AD_REMESSAORIGEM") != null) {
         BigDecimal previous = OldVo.asBigDecimal("QTDNEG");
         DynamicVO ite = (DynamicVO)event.getVo();
         BigDecimal later = ite.asBigDecimal("QTDNEG");
         if (previous.compareTo(later) < 0) {
            throw new MGEModelException("Atenção: para aumentar a quantidade de itens use o dashboard");
         }
      }

   }
}