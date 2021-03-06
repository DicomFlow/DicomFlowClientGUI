package br.ufpb.dicomflow.gui.application;



import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import br.ufpb.dicomflow.gui.business.DownloadProcessor;
import br.ufpb.dicomflow.gui.business.MessageProcessor;
import br.ufpb.dicomflow.gui.components.MessageTreeItem;
import br.ufpb.dicomflow.gui.dao.bean.MessageBean;
import br.ufpb.dicomflow.gui.exception.DownloadException;
import br.ufpb.dicomflow.gui.exception.MessageException;
import br.ufpb.dicomflow.integrationAPI.mail.MessageIF;
import br.ufpb.dicomflow.integrationAPI.message.xml.Patient;
import br.ufpb.dicomflow.integrationAPI.message.xml.RequestPut;
import br.ufpb.dicomflow.integrationAPI.message.xml.ServiceIF;
import br.ufpb.dicomflow.integrationAPI.message.xml.Study;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ReceivedMessagesController implements Initializable {

	private static final double SPACING = 0;
	private static final double COLLAPSE_TREVIEW = 30.0;
	private static final double EXPAND_TREVIEW = 120.0;
	@FXML
	private VBox messageVBox;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		messageVBox.setSpacing(SPACING);

		List<MessageBean> messages = loadMessages();

		for (MessageBean messageBean: messages) {

			TreeView<MessageTreeItem> treeView = new TreeView<MessageTreeItem>();
			treeView.setPrefHeight(COLLAPSE_TREVIEW);


			TreeItem<MessageTreeItem> messageTreeItem = createMessageTreeItem(messageBean);

			messageTreeItem.expandedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					BooleanProperty bproperty = (BooleanProperty) observable;
					if(bproperty.getValue()){

						//update status
						MessageProcessor.getInstance().changeMessageStatus(messageTreeItem.getValue().getIdMessage(), MessageBean.READ);

						ImageView statusIcon = messageTreeItem.getValue().getStatusImage();
						statusIcon.setImage(new Image(getClass().getResourceAsStream("img/reading_16.png")));
						SceneLoader.getInstance().installTooltip(statusIcon, "Lida");

						//expand treeView
						treeView.setPrefHeight(EXPAND_TREVIEW);
						SceneLoader.getInstance().expandTreeView(messageTreeItem);
					}else{

						//collapse treeView
						treeView.setPrefHeight(COLLAPSE_TREVIEW);
						SceneLoader.getInstance().collapseTreeView(messageTreeItem);
					}
				}
			});


			TreeItem<MessageTreeItem> serviceTreeItem = serviceTreeItem(messageBean);
			if(serviceTreeItem != null){
				messageTreeItem.getChildren().add(serviceTreeItem);
			}

			treeView.setRoot(messageTreeItem);
			messageVBox.getChildren().add(treeView);

        }



	}

	public TreeItem<MessageTreeItem> serviceTreeItem(MessageBean messageBean)  {


		try {
			MessageIF message = messageBean.getValues();

			ServiceIF service = message.getService();

			if(service instanceof RequestPut){
				return createRequestPutTreeItem(messageBean, service);
			}

		} catch (IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
			e.printStackTrace();
		}



		return null;
	}

	public TreeItem<MessageTreeItem> createRequestPutTreeItem(MessageBean messageBean, ServiceIF service) {
		RequestPut requestPut = (RequestPut) service;


		Button downloadButton =  new Button("Baixar Imagens");
		downloadButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Main.getpStage().getScene().setCursor(Cursor.WAIT);

				try {
					String filename = DownloadProcessor.getInstance().download(requestPut);

					SceneLoader.getInstance().informationAlert("Download", "Informa��o", "Download efetudo em: " + filename);

				} catch (DownloadException e) {
					Main.getpStage().getScene().setCursor(Cursor.DEFAULT);
					e.printStackTrace();
					SceneLoader.getInstance().informationAlert("Download", "Erro", e.getMessage());
				}

				Main.getpStage().getScene().setCursor(Cursor.DEFAULT);

			}
		});


		Button replyButton =  new Button("Enviar Laudo");
		replyButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Main.getpStage().getScene().setCursor(Cursor.WAIT);
				try {

					FileChooser directoryChooser = new FileChooser();
		            File selectedFile =
		                    directoryChooser.showOpenDialog(Main.getpStage());

		            if(selectedFile != null){

		            	MessageProcessor.getInstance().sendReport(messageBean, requestPut, selectedFile);

		            	SceneLoader.getInstance().informationAlert("Laudo", "Informa��o", "Laudo enviado com sucesso.");
		            }

				} catch (MessageException e) {
					Main.getpStage().getScene().setCursor(Cursor.DEFAULT);
					e.printStackTrace();
					SceneLoader.getInstance().informationAlert("Laudo", "Erro", e.getMessage());
				}

				Main.getpStage().getScene().setCursor(Cursor.DEFAULT);

			}
		});

		TreeItem<MessageTreeItem> requestPutTreeItem = new TreeItem<MessageTreeItem>(new MessageTreeItem( new Label("Requisi��o - ID: " + requestPut.getMessageID()), downloadButton, replyButton));


		for (Patient patient: requestPut.getUrl().getPatient()) {
			TreeItem<MessageTreeItem> patientList = new TreeItem<MessageTreeItem>(new MessageTreeItem( new Label("Paciente: " + patient.getName()), new Label( "Nascicmento: " + patient.getBirthdate() ), new Label( "G�nero: " + patient.getGender() ),new ImageView(new Image(getClass().getResourceAsStream("img/avatar_16.png")))) );
			for (Study study: patient.getStudy()) {
				TreeItem<MessageTreeItem> studyList = new TreeItem<MessageTreeItem>(new MessageTreeItem( new Label( "Estudo: " + study.getDescription() ), new ImageView(new Image(getClass().getResourceAsStream("img/test_16.png")))) );
				patientList.getChildren().add(studyList);
			}
			requestPutTreeItem.getChildren().add(patientList);
		}
		return requestPutTreeItem;
	}

	public TreeItem<MessageTreeItem> createMessageTreeItem(MessageBean messageBean) {


		String image = messageBean.getStatuss().equals(MessageBean.UNREAD) ? "img/mail_16.png" : "img/reading_16.png";
		ImageView statusIcon = new ImageView(new Image(getClass().getResourceAsStream(image)));

		String tooltipText = messageBean.getStatuss().equals(MessageBean.UNREAD) ? "N�o lida" : "Lida";
		SceneLoader.getInstance().installTooltip(statusIcon, tooltipText);

		TreeItem<MessageTreeItem> messageTreeItem  = new TreeItem<MessageTreeItem>(new MessageTreeItem( new Label(messageBean.getFromm()), new Label(messageBean.getSubjectt()),statusIcon, messageBean.getIdentifierValue()));
		return messageTreeItem;
	}



	public List<MessageBean> loadMessages(){


		List<MessageBean> messages = MessageProcessor.getInstance().loadReceivedMessages(ApplicationSession.getInstance().getLoggedUser(), MessageProcessor.FIRST_PAGE, MessageProcessor.DEFAULT_MAX);
		return messages;

	}

}
