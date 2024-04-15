package eventoop;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

public class Barraupdate  implements EventoProgramavelJava{

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		// ANTES
		DynamicVO OldVo = (DynamicVO) event.getOldVO();

		if (OldVo.asBigDecimal("AD_REMESSAORIGEM") != null) {

			BigDecimal previous = OldVo.asBigDecimal("QTDNEG");

			// DEPOIS
			DynamicVO ite = (DynamicVO) event.getVo();

			BigDecimal later = ite.asBigDecimal("QTDNEG");
			
			
			if (previous.compareTo(later) < 0) {
				throw new MGEModelException(("Atenção: para aumentar a quantidade de itens use o dashboard"));
			}
		}
		}
	}


