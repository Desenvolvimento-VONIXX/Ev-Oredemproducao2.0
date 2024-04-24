package eventoop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.mgecomercial.model.facades.helpper.FaturamentoHelper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class Utilitario_HTML {

	


	//########################   FUNÇÕES DO DASHBOARD
	//Lança o produto na nota gerada
	public void additensLog(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, String codvol, BigDecimal vlrunit, BigDecimal vlrtotal, BigDecimal remessaOrigem, BigDecimal sequencia, BigDecimal apontamento, BigDecimal volume,BigDecimal nunpedevox ) throws Exception {
			
			
			ImpostosHelpper imp = new ImpostosHelpper();
			
			JapeSession.SessionHandle hnd = null;
			try {
				
				JapeWrapper proDAO = JapeFactory.dao("Produto");
				JapeWrapper iteDAO = JapeFactory.dao("ItemNota");

				        iteDAO.create()
						.set("CODEMP", BigDecimal.valueOf(1))
						.set("CODPROD", codprod)
						.set("NUNOTA", nunota)
						.set("QTDNEG", qtdneg)
						.set("CODVOL", proDAO.findByPK(codprod).asString("CODVOL"))
						.set("VLRUNIT", vlrunit)
						.set("VLRTOT", vlrtotal)
						.set("CODLOCALORIG", BigDecimal.valueOf(1030000))
						.set("RESERVA", "N")
						.set("ATUALESTOQUE", BigDecimal.valueOf(-1))
						.set("CODVEND",BigDecimal.valueOf(0))
						.set("VLRDESC", BigDecimal.valueOf(0.00))
						.set("CODCFO", BigDecimal.valueOf(5905))
						.set("CODTRIB", BigDecimal.valueOf(41))
						.set("PERCDESC", BigDecimal.valueOf(0.00))
						.set("BASEICMS", BigDecimal.valueOf(0.00))
						.set("VLRICMS",  BigDecimal.valueOf(0.00))
						.set("ALIQICMS",  BigDecimal.valueOf(0.00))
						.set("BASEIPI",  BigDecimal.valueOf(0.00))
						.set("VLRIPI",  BigDecimal.valueOf(0.00))
						.set("ALIQIPI",  BigDecimal.valueOf(0.00))
						.set("CODLOCALTERC",  BigDecimal.valueOf(1030000))
						.set("ATUALESTTERC", "P")
						.set("TERCEIROS", "S")
						.set("PENDENTE",  "S")
						.set("AD_NUNPEDEVOX", nunpedevox)
						.set("AD_REMESSAORIGEM", remessaOrigem)
						.set("AD_CUSTOMIZACAO",  BigDecimal.valueOf(24))//CUSTOMIZAÇÃO RECEBE O CAMPO DA NOTA PARA CASO DELETE RETORNAR PARA O VALOR CERTO
				        .save();
				        
				        //subtrai da nota de origem (24)
				        subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg,remessaOrigem);
				        	//verifica se o volume é de 20 Litros para lançar bombona
				          if(volume.intValue() ==6) {
				        	  adicionaBombona(nunota,qtdneg,remessaOrigem);
				          }
		        	
				imp.calcularImpostos(nunota);
			    imp.setForcarRecalculo(true);
			    imp.totalizarNota(nunota);
			    Adicionaitem add = new Adicionaitem();
				BigDecimal vlrtotCab=add.selecionarItens(BigDecimal.valueOf(24));
				add.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24));

			}
			
			 catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);

			} finally {
			
				
			}
			
			
			
		}

	
	
	//Verifica se o produto está na nota, se sim retorna true
	public boolean verificaNota(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal sequencia, BigDecimal apontamento, BigDecimal remessaOrigem, BigDecimal volume) throws MGEModelException {
	    
		boolean resultado = false;
	    SessionHandle hnd = null;
	    JdbcWrapper jdbc = null;
	    NativeSql query = null;
	    ResultSet rset = null;
	    
	    System.out.println("função verificaNota");

	    //Pega a quantidade anterior na nota que foi criada
	    try {
	       hnd = JapeSession.open();
	       hnd.setFindersMaxRows(-1);
	       EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	       jdbc = entity.getJdbcWrapper();
	       jdbc.openSession();
	       query = new NativeSql(jdbc);
	       query.setNamedParameter("CODPROD", codprod);
	       query.setNamedParameter("NUNOTA", nunota);
	       query.appendSql("SELECT QTDNEG FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
	       rset = query.executeQuery();
	       if (rset.next()) {
	          
	    	   resultado = true;
	          BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
	          //função que soma na nota que foi criada
	          somaProd(nunota, codprod, qtdneg, qtdAnterior,volume,remessaOrigem);
	          //função que subtrai da nota origem (24)
	          this.subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg, remessaOrigem);
	       }
	    } catch (Exception var17) {
	       var17.printStackTrace();
	       MGEModelException.throwMe(var17);
	       System.out.println("Erro ao Executar consultaPreco" + var17.getCause() + var17.getMessage());
	    } finally {
	       JdbcWrapper.closeSession(jdbc);
	       JapeSession.close(hnd);
	    }

	    return resultado;
	 }
	
public boolean verificaNotaEVOX(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal sequencia, BigDecimal apontamento, BigDecimal remessaOrigem, BigDecimal volume, BigDecimal nunpedevox) throws MGEModelException {
	    
		boolean resultado = false;
	    SessionHandle hnd = null;
	    JdbcWrapper jdbc = null;
	    NativeSql query = null;
	    ResultSet rset = null;
	    
	    System.out.println("função verificaNota");

	    try {
	       hnd = JapeSession.open();
	       hnd.setFindersMaxRows(-1);
	       EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	       jdbc = entity.getJdbcWrapper();
	       jdbc.openSession();
	       query = new NativeSql(jdbc);
	       query.setNamedParameter("CODPROD", codprod);
	       query.setNamedParameter("NUNOTA", nunota);
	       query.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
	       query.appendSql("SELECT QTDNEG FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
	       rset = query.executeQuery();
	       if (rset.next()) {
	          
	    	  resultado = true;
	          BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
	          somaProdEVOX(nunota, codprod, qtdneg, qtdAnterior,volume,remessaOrigem,nunpedevox);
	          this.subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg, remessaOrigem);
	       }
	    } catch (Exception var17) {
	       var17.printStackTrace();
	       MGEModelException.throwMe(var17);
	       System.out.println("Erro ao Executar consultaPreco" + var17.getCause() + var17.getMessage());
	    } finally {
	       JdbcWrapper.closeSession(jdbc);
	       JapeSession.close(hnd);
	    }

	    return resultado;
	 }
	
	
	
	

	 public void subtrairDoApontamento(BigDecimal codprod, BigDecimal sequencia, BigDecimal apontamento, BigDecimal qtdneg, BigDecimal remessaOrigem) throws MGEModelException {
	    SessionHandle hnd = null;
	    JdbcWrapper jdbc = null;
	    NativeSql query = null;
	    ResultSet rset = null;
	    //Utilitarios util = new Utilitarios();
	    System.out.println(" ###################achou subtrairDoApontamento ");

	    try {
	       hnd = JapeSession.open();
	       hnd.setFindersMaxRows(-1);
	       EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	       jdbc = entity.getJdbcWrapper();
	       jdbc.openSession();
	       query = new NativeSql(jdbc);
	       query.setNamedParameter("CODPROD", codprod);
	       query.setNamedParameter("SEQUENCIA", sequencia);
	       query.setNamedParameter("NUNOTA", remessaOrigem);
	       query.appendSql("SELECT QTDNEG,NUNOTA FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND SEQUENCIA = :SEQUENCIA");
	       rset = query.executeQuery();
	       if (rset.next()) {
	          BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
	          BigDecimal nunota = rset.getBigDecimal("NUNOTA");
	          subtraiProd(nunota, codprod, qtdneg, qtdAnterior, sequencia,remessaOrigem);
	          Adicionaitem add = new Adicionaitem();
			  BigDecimal vlrtotCab=add.selecionarItens(BigDecimal.valueOf(24));
			  add.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24));
	       }
	    } catch (Exception var16) {
	       var16.printStackTrace();
	       MGEModelException.throwMe(var16);
	       System.out.println("Erro ao Executar consultaPreco" + var16.getCause() + var16.getMessage());
	    } finally {
	       JdbcWrapper.closeSession(jdbc);
	       JapeSession.close(hnd);
	    }

	 }
	
	
	
	
	
	//############################## FUNÇÕES DO DASHBOARD HTML
		 public void somaProd(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal volume, BigDecimal remessaOrigem) throws Exception {
		      BigDecimal novavariavel = qtdneg.add(qtdAnterior);
		      ImpostosHelpper imp = new ImpostosHelpper();
		      
		      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		      JdbcWrapper jdbc = entity.getJdbcWrapper();
		      jdbc.openSession();
		      NativeSql query = new NativeSql(jdbc);
		      query.setNamedParameter("NUNOTA", nunota);
		      query.setNamedParameter("CODPROD", codprod);
		      query.appendSql("SELECT VLRUNIT FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
		      ResultSet rset = query.executeQuery();
		      if (rset.next()) {
		         BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");

		         try {
		            NativeSql sql = new NativeSql(jdbc);
		            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
		            sql.setNamedParameter("NUNOTA", nunota);
		            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
		            sql.setNamedParameter("QUANTIDADE", novavariavel);
		            sql.setNamedParameter("CODPROD", codprod);
		            sql.executeUpdate();
		            
		            
		            if(volume.intValue() ==6) {
		          	  adicionaBombona(nunota,qtdneg,remessaOrigem);
		            }
		            imp.calcularImpostos(nunota);
		            imp.setForcarRecalculo(true);
		            imp.totalizarNota(nunota);
		            
		         } catch (Exception var17) {
		            var17.printStackTrace();
		         } finally {
		            JdbcWrapper.closeSession(jdbc);
		          
		         }
		      }

		   }
		 
		 public void somaProdEVOX(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal volume, BigDecimal remessaOrigem, BigDecimal nunpedevox) throws Exception {
		      BigDecimal novavariavel = qtdneg.add(qtdAnterior);
		      ImpostosHelpper imp = new ImpostosHelpper();
		      
		      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		      JdbcWrapper jdbc = entity.getJdbcWrapper();
		      jdbc.openSession();
		      NativeSql query = new NativeSql(jdbc);
		      query.setNamedParameter("NUNOTA", nunota);
		      query.setNamedParameter("CODPROD", codprod);
		      query.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
		      query.appendSql("SELECT VLRUNIT FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
		      ResultSet rset = query.executeQuery();
		      if (rset.next()) {
		         BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");

		         try {
		            NativeSql sql = new NativeSql(jdbc);
		            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
		            sql.setNamedParameter("NUNOTA", nunota);
		            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
		            sql.setNamedParameter("QUANTIDADE", novavariavel);
		            sql.setNamedParameter("CODPROD", codprod);
		            sql.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
		            sql.executeUpdate();
		            
		            
		            if(volume.intValue() ==6) {
		          	  adicionaBombona(nunota,qtdneg,remessaOrigem);
		            }
		            imp.calcularImpostos(nunota);
		            imp.setForcarRecalculo(true);
		            imp.totalizarNota(nunota);
		            
		         } catch (Exception var17) {
		            var17.printStackTrace();
		         } finally {
		            JdbcWrapper.closeSession(jdbc);
		          
		         }
		      }

		   }
		 



		public void subtraiProd(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal remessaOrigem) throws Exception {
		      BigDecimal novavariavel = qtdAnterior.subtract(qtdneg);
		      String where = "";
		      
		      if(novavariavel.intValue() == 0) {
		    	  where = " DELETE FROM TGFITE WHERE NUNOTA = :NUNOTA AND SEQUENCIA = :SEQUENCIA ";
		      } else {
		    	  where = " UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA ";
		      }
		      ImpostosHelpper imp = new ImpostosHelpper();
		      
		      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		      JdbcWrapper jdbc = entity.getJdbcWrapper();
		      jdbc.openSession();
		      NativeSql query = new NativeSql(jdbc);
		      query.setNamedParameter("CODPROD", codprod);
//		      query.setNamedParameter("NUNOTA", remessaOrigem);
		      query.appendSql("SELECT VLRVENDA FROM TGFEXC, TGFTAB WHERE TGFTAB.NUTAB = TGFEXC.NUTAB AND TGFTAB.CODTAB = 6"
						+ "AND  DTVIGOR =  (SELECT MAX( DTVIGOR) FROM TGFTAB TAB1 WHERE TGFTAB.CODTAB = TAB1.CODTAB AND CODPROD = :CODPROD)");
		      ResultSet rset = query.executeQuery();
		      if (rset.next()) {
		         BigDecimal vlrunitario = rset.getBigDecimal("VLRVENDA");

		         try {
		            NativeSql sql = new NativeSql(jdbc);
		            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA");
		            sql.setNamedParameter("SEQUENCIA", sequencia);
		            sql.setNamedParameter("NUNOTA", remessaOrigem);
		            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
		            sql.setNamedParameter("QUANTIDADE", novavariavel);
		            sql.setNamedParameter("CODPROD", codprod);
		            sql.executeUpdate();
		            imp.calcularImpostos(nunota);
		            imp.setForcarRecalculo(true);
		            imp.totalizarNota(nunota);
		         } catch (Exception var18) {
		            var18.printStackTrace();
		         } finally {
		            JdbcWrapper.closeSession(jdbc);
		           
		         }
		      }

		   }

		   public void atualizaNotaOrigem(BigDecimal nunotaOrigem, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal codprod, BigDecimal vlrunitario) throws Exception {
		      BigDecimal novoQTD = qtdneg.add(qtdAnterior);
		      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		      JdbcWrapper jdbc = entity.getJdbcWrapper();
		      jdbc.openSession();

		      try {
		         NativeSql sql = new NativeSql(jdbc);
		         sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA");
		         sql.setNamedParameter("SEQUENCIA", sequencia);
		         sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novoQTD));
		         sql.setNamedParameter("QUANTIDADE", novoQTD);
		         sql.setNamedParameter("CODPROD", codprod);
		         sql.setNamedParameter("NUNOTA", nunotaOrigem);
		         sql.executeUpdate();
		      } catch (Exception var14) {
		         var14.printStackTrace();
		      } finally {
		         JdbcWrapper.closeSession(jdbc);
		      }

		   }
		   
		   public void atualizaNotaOrigemEVOX(BigDecimal nunotaOrigem, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal codprod, BigDecimal vlrunitario, BigDecimal nunpedevox) throws Exception {
			      BigDecimal novoQTD = qtdneg.add(qtdAnterior);
			      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			      JdbcWrapper jdbc = entity.getJdbcWrapper();
			      jdbc.openSession();

			      try {
			         NativeSql sql = new NativeSql(jdbc);
			         sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
			         sql.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
			         sql.setNamedParameter("SEQUENCIA", sequencia);
			         sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novoQTD));
			         sql.setNamedParameter("QUANTIDADE", novoQTD);
			         sql.setNamedParameter("CODPROD", codprod);
			         sql.setNamedParameter("NUNOTA", nunotaOrigem);
			         sql.executeUpdate();
			      } catch (Exception var14) {
			         var14.printStackTrace();
			      } finally {
			         JdbcWrapper.closeSession(jdbc);
			      }

			   }


		   public void confirmaPedidoSnk(BigDecimal nuNota) throws MGEModelException {
			   JapeSession.SessionHandle hnd = null;
				try {
					hnd = JapeSession.open(); //ABERTURA DE SESSÃO
					
					hnd.execWithTX( new JapeSession.TXBlock(){//BLOCO DE TRANSAÇÃO MANUAL
		        		public void doWithTx() throws Exception{//BLOCO DE TRANSAÇÃO MANUAL
		         AuthenticationInfo authenticationInfo = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
		         authenticationInfo.makeCurrent();
		         AuthenticationInfo.getCurrent();
		         BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
		         barramentoConfirmacao.setValidarSilencioso(true);
		         ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
		        		}
		        		
		    			;}); //FECHA BLOCO DE TRANSAÇÃO MANUAL
		    			 
		    			
		    			JapeSession.close(hnd); //FECHA SESSÃO DE TRANSAÇÃO MANUAL
		      } catch (Exception var4) {
		         var4.printStackTrace();
		         MGEModelException.throwMe(var4);
		      }

		   }
		   
		   
		   
		 //FATURA NOTA
			public void GerarNota(BigDecimal nunota, BigDecimal top) throws Exception {
				
				//CONFIG FATURAMENTO
				CentralFaturamento cent = new CentralFaturamento();
		        cent.abreCabecalhoNotaOrigem(nunota, false);
		        CentralFaturamento.ConfiguracaoFaturamento cf = cent.getConfiguracaoFaturamento();
		        try {
		        	//TOP E TIPO DE NEGOCIACAO
		            JapeWrapper codtipoperDao = JapeFactory.dao("TipoOperacao");
		            DynamicVO codtipoperVo = codtipoperDao.findOne("CODTIPOPER=?", new Object[]{top});
		            cf.setTipMovDest(codtipoperVo.asString("TIPMOV"));
		            cf.setUsaTopDestino(false);
		            cf.isIncluirNotaPendente();
		            cf.setSerie("1");
		            cf.setConfirmarNota(true);
		            cf.setCodTipOper(top);

		            //NOTAS PARA FATURAR
		            Collection<BigDecimal> notasNunota = new ArrayList<>();
		            notasNunota.add(nunota);
		            
		            //ABERTURA DE SESSAO
		           

//	    			hnd = ; //ABERTURA DE SESSÃO
		    		try {
			            
		    			final JapeSession.SessionHandle hnd = JapeSession.open();
		    			hnd.execWithTX( new JapeSession.TXBlock(){//BLOCO DE TRANSAÇÃO MANUAL
		            		public void doWithTx() throws Exception{//BLOCO DE TRANSAÇÃO MANUAL
		            			
		                cent.getConfiguracaoFaturamento().getTipMovDest();
		                Map<BigDecimal, BigDecimal> m = new HashMap<>();
		                m.put(nunota, BigDecimal.valueOf(0));

		                //FATURA HELPER
		                AuthenticationInfo auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
		                auth.makeCurrent();
		                FaturamentoHelper.faturar(ServiceContext.getCurrent(), hnd, cf, notasNunota, m); 
		            		}
		            		
		        			;}); //FECHA BLOCO DE TRANSAÇÃO MANUAL
		        			 
		        			
		        			JapeSession.close(hnd); //FECHA SESSÃO DE TRANSAÇÃO MANUAL
		                
		           
		            } finally {
		          
		            }
		       
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new Exception("Erro ao faturar pedido: " + e);
		}
		}
			
			
			
			
//			  ADICIONAR BOMBONA DE 20L no pedido gerado
			   public void adicionaBombona(BigDecimal nunota, BigDecimal qtdneg, BigDecimal remessaOrigem) throws MGEModelException {

				   boolean existeBombona = false;
				   SessionHandle hnd = null;
				    JdbcWrapper jdbc = null;
				    NativeSql query = null;
				    ResultSet rset = null;
				    
				    System.out.println("função verificaNota");
				    //Verifica se já aexite BOMBONA  na nota, para poder adicionar
				    try {
				       hnd = JapeSession.open();
				       
				       EntityFacade entity = EntityFacadeFactory.getDWFFacade();
				       jdbc = entity.getJdbcWrapper();
				       jdbc.openSession();
				       query = new NativeSql(jdbc); 
				       query.setNamedParameter("NUNOTA", nunota);
				       query.appendSql("SELECT QTDNEG,VLRUNIT FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001");
				       rset = query.executeQuery();
				       if (rset.next()) {
				    	   existeBombona = true;
				    	   
				    	   BigDecimal qtdnegAnterior = rset.getBigDecimal("QTDNEG");
				    	   BigDecimal novavariavel = qtdnegAnterior.add(qtdneg);
				    	   BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");
				    	    
				            NativeSql sql = new NativeSql(jdbc);
				            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT  WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001");
				            sql.setNamedParameter("NUNOTA", nunota);
				            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
				            sql.setNamedParameter("QUANTIDADE", novavariavel);
				            sql.executeUpdate();
				            
				            
				          }
				       //se não existir, deve lançar BOMBONA na nota
				       if(!existeBombona) {
				    	   lancarBombona(nunota,qtdneg,remessaOrigem);
				       }
				       //sbtrai bombona da nota de origem
				       subtraiBombonaApontamento(remessaOrigem,qtdneg);
				       
				    } catch (Exception var17) {
				       var17.printStackTrace();
				       MGEModelException.throwMe(var17);
				       System.out.println("Erro ao Executar consultaPreco" + var17.getCause() + var17.getMessage());
				    } finally {
				       JdbcWrapper.closeSession(jdbc);
				       JapeSession.close(hnd);
				    }
			
		}

			private void subtraiBombonaApontamento(BigDecimal remessaOrigem, BigDecimal qtdneg) throws MGEModelException {
		
				   SessionHandle hnd = null;
				    JdbcWrapper jdbc = null;
				    NativeSql query = null;
				    ResultSet rset = null;
				    
				    

				    try {
				       hnd = JapeSession.open();
				       
				       EntityFacade entity = EntityFacadeFactory.getDWFFacade();
				       jdbc = entity.getJdbcWrapper();
				       jdbc.openSession();
				       query = new NativeSql(jdbc); 
				       query.setNamedParameter("QTDNEG", qtdneg);
				       query.setNamedParameter("NUNOTA", remessaOrigem);
				       query.appendSql("SELECT TOP 1 QTDNEG,VLRUNIT,SEQUENCIA FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001 AND QTDNEG=:QTDNEG");
				       rset = query.executeQuery();
				       if (rset.next()) {

				    	   
				    	   BigDecimal qtdnegAnterior = rset.getBigDecimal("QTDNEG");
				    	   BigDecimal sequencia = rset.getBigDecimal("SEQUENCIA");
				    	   BigDecimal novavariavel = qtdnegAnterior.subtract(qtdneg);
				    	   BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");
				            NativeSql sql = new NativeSql(jdbc);
				            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT  WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001 AND SEQUENCIA=:SEQUENCIA");
				            sql.setNamedParameter("NUNOTA", remessaOrigem);
				            sql.setNamedParameter("SEQUENCIA", sequencia);
				            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
				            sql.setNamedParameter("QUANTIDADE", novavariavel);
				            sql.executeUpdate();
				            
				            
				        }
				       
				       
				      
				       
				    } catch (Exception var17) {
				       var17.printStackTrace();
				       MGEModelException.throwMe(var17);
				       System.out.println("Erro ao Executar consultaPreco" + var17.getCause() + var17.getMessage());
				    } finally {
				       JdbcWrapper.closeSession(jdbc);
				       JapeSession.close(hnd);
				    }
		
	}
			
			public void lancarBombona(BigDecimal nunota,  BigDecimal qtdneg,   BigDecimal remessaOrigem) throws Exception {
				
				BigDecimal codprod = BigDecimal.valueOf(4008001);
				String where = "CODPROD = 4008001 AND NUNOTA= "+remessaOrigem;
				BigDecimal vlrunit = NativeSql.getBigDecimal("VLRUNIT", "TGFITE", where);
				BigDecimal vlrtotal = qtdneg.multiply(vlrunit);
				ImpostosHelpper imp = new ImpostosHelpper();
				
				JapeSession.SessionHandle hnd = null;
				try {
					
					JapeWrapper proDAO = JapeFactory.dao("Produto");
					JapeWrapper iteDAO = JapeFactory.dao("ItemNota");

					        iteDAO.create()
							.set("CODEMP", BigDecimal.valueOf(1))
							.set("CODPROD", codprod)
							.set("NUNOTA", nunota)
							.set("QTDNEG", qtdneg)
							.set("CODVOL", proDAO.findByPK(codprod).asString("CODVOL"))
							.set("VLRUNIT", vlrunit)
							.set("VLRTOT", vlrtotal)
							.set("CODLOCALORIG", BigDecimal.valueOf(1030000))
							.set("RESERVA", "N")
							.set("ATUALESTOQUE", BigDecimal.valueOf(-1))
							.set("CODVEND",BigDecimal.valueOf(0))
							.set("VLRDESC", BigDecimal.valueOf(0.00))
							.set("CODCFO", BigDecimal.valueOf(5905))
							.set("CODTRIB", BigDecimal.valueOf(41))
							.set("PERCDESC", BigDecimal.valueOf(0.00))
							.set("BASEICMS", BigDecimal.valueOf(0.00))
							.set("VLRICMS",  BigDecimal.valueOf(0.00))
							.set("ALIQICMS",  BigDecimal.valueOf(0.00))
							.set("BASEIPI",  BigDecimal.valueOf(0.00))
							.set("VLRIPI",  BigDecimal.valueOf(0.00))
							.set("ALIQIPI",  BigDecimal.valueOf(0.00))
							.set("CODLOCALTERC",  BigDecimal.valueOf(1030000))
							.set("ATUALESTTERC", "P")
							.set("TERCEIROS", "S")
							.set("PENDENTE",  "S")
							.set("AD_CUSTOMIZACAO",  BigDecimal.valueOf(24))//CUSTOMIZAÇÃO RECEBE O CAMPO DA NOTA PARA CASO DELETE RETORNAR PARA O VALOR CERTO
							.set("AD_REMESSAORIGEM", remessaOrigem)
					        .save();
					
					  System.out.println("###################função additensLog");
					  
					imp.calcularImpostos(nunota);
				    imp.setForcarRecalculo(true);
				    imp.totalizarNota(nunota);
					

		        	
				}
				
				 catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);

				} finally {
				
					
				}
				
				
				
			}		
			
			
}
