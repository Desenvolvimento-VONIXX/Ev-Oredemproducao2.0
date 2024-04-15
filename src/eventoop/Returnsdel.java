package eventoop;

import java.math.BigDecimal;
import java.sql.ResultSet;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Returnsdel implements EventoProgramavelJava{

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
		//VERIFICA SE O ITEM EXCLUINDO FOI GERADO PELO DASH
		DynamicVO ite = (DynamicVO) event.getVo();
		
		BigDecimal later = ite.asBigDecimal("QTDNEG");
		BigDecimal codprod = ite.asBigDecimal("CODPROD");
		BigDecimal nunotaOrigem = ite.asBigDecimal("AD_REMESSAORIGEM");
		BigDecimal customizacao = ite.asBigDecimal("AD_CUSTOMIZACAO");
		BigDecimal nunota = ite.asBigDecimal("NUNOTA");
		
		if(nunotaOrigem!=null && customizacao.intValue() == 24) {
			//somaregistro(nunota,codprod,later);
			
			pegaIdLog(nunota,codprod,nunotaOrigem);
			
			
			
		}
	}

	private void pegaIdLog(BigDecimal nunota, BigDecimal codprod, BigDecimal nunotaOrigem) throws MGEModelException {
		
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		SessionHandle hnd = JapeSession.open();
		BigDecimal idLog = BigDecimal.ZERO;
		
		try {

			String sql1 = "SELECT IDAPONTAMENTO FROM AD_LOGAPONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD =:CODPROD";
			NativeSql query1 = new NativeSql(jdbc);
			query1.setNamedParameter("CODPROD", codprod);
			query1.setNamedParameter("NUNOTA", nunota);
			query1.appendSql(sql1);
			ResultSet rset1 = query1.executeQuery();
			
		    while (rset1.next()) {
		    	 idLog = rset1.getBigDecimal("IDAPONTAMENTO"); 	
		    	 atualizaLogCancelamento(nunota,nunotaOrigem,codprod,idLog);
		    	 
	        }

		    
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	    	   JapeSession.close(hnd);
				JdbcWrapper.closeSession(jdbc);
	    }
		
		
	}

	private void atualizaLogCancelamento(BigDecimal nunota, BigDecimal nunotaOrigem, BigDecimal codprod, BigDecimal idApontamento) throws MGEModelException {
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		SessionHandle hnd = JapeSession.open();
		

		try {
//			String where = "CODPROD = "+codprod+" AND NUNOTA= "+nunota;
//			BigDecimal idLog = NativeSql.getBigDecimal("IDAPONTAMENTO", "AD_LOGAPONTAMENTO", where);
			
			
			
        	JapeFactory.dao("AD_LOGAPONTAMENTO").prepareToUpdateByPK(idApontamento)
			.set("DTCANCELAMENTO", TimeUtils.getNow())
    		.update(); 
			
//	    	String updateSql = "UPDATE AD_LOGAPONTAMENTO SET DTCANCELAMENTO = :DTCANCELAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD";
//            NativeSql updateQuery = new NativeSql(jdbc);
//            updateQuery.setNamedParameter("CODPROD", codprod);
//            updateQuery.setNamedParameter("NUNOTA", nunota);
//            updateQuery.setNamedParameter("DTCANCELAMENTO", TimeUtils.getNow());
//            updateQuery.appendSql(updateSql);
//            updateQuery.executeUpdate();
			
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	    	   JapeSession.close(hnd);
				JdbcWrapper.closeSession(jdbc);
	    }
		
		
	}

	private void somaregistro(BigDecimal nunota, BigDecimal codprod, BigDecimal later) throws Exception {
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		SessionHandle hnd = JapeSession.open();
		ImpostosHelpper imp = new ImpostosHelpper();
		Adicionaitem add = new Adicionaitem();
		
		
		try {

			String sql1 = "SELECT QTDNEG FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD =:CODPROD";
			NativeSql query1 = new NativeSql(jdbc);
			query1.setNamedParameter("CODPROD", codprod);
			query1.setNamedParameter("NUNOTA", nunota);
			query1.appendSql(sql1);
			ResultSet rset1 = query1.executeQuery();
			
		    if (rset1.next()) {
		    	BigDecimal qtdbefore = rset1.getBigDecimal("QTDNEG");
		    	
		    	BigDecimal qtdfinal = qtdbefore.add(later);
		    
		    	String updateSql = "UPDATE TGFITE SET QTDNEG = :QTDFINAL WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD";
	            NativeSql updateQuery = new NativeSql(jdbc);
	            updateQuery.setNamedParameter("CODPROD", codprod);
	            updateQuery.setNamedParameter("NUNOTA", nunota);
	            updateQuery.setNamedParameter("QTDFINAL", qtdfinal);
	            updateQuery.appendSql(updateSql);
	            updateQuery.executeUpdate();
	            
	            imp.calcularImpostos(nunota);
			    imp.setForcarRecalculo(true);
			    imp.totalizarNota(nunota);
	        }
		    
		    
		    
		    
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	    	   JapeSession.close(hnd);
				JdbcWrapper.closeSession(jdbc);
	    }
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
