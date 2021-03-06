package application.controller;

import application.Main;
import application.controller.optiontabs.CodegenOptsController;
import application.controller.optiontabs.CompilerOptsController;
import application.controller.optiontabs.DebuggingOptsController;
import application.controller.optiontabs.DeveloperOptsController;
import application.controller.optiontabs.ExecuteOptsController;
import application.controller.optiontabs.LinkingOptsController;
import application.controller.optiontabs.OptimizationOptsController;
import application.controller.optiontabs.RunningProjectController;
import application.model.CommandExecute;
import application.model.Project;
import application.model.SmartModel;
import application.views.RulesTextField;
import application.views.TabProjectPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** This is the main controller of the application. It can call other controllers. */
public class MainController {

  SmartModel smartModel;
  public CommandExecute commandExecute;
  LinkedList<TabProjectPane> projects;
  @FXML private Button runOption;
  @FXML private Button compileOption;
  @FXML private TabPane panelCompilingOptions;

  int type;

  @FXML private TabPane projectsPane;
  @FXML Tab textResult;

  StackPane secondaryLayout;
  StackPane helpLayout;

  TextField inputProjectDirectory;
  RulesTextField inputProjectName;
  TextArea textAreaFiles;
  private TextArea textAreaResult;

  // Option-Tab Components and Controllers
  @FXML private Parent compilerOpts;
  @FXML private CompilerOptsController compilerOptsController;
  @FXML private Parent linkingOpts;
  @FXML private LinkingOptsController linkingOptsController;
  @FXML private Parent debuggingOpts;
  @FXML private DebuggingOptsController debuggingOptsController;
  @FXML private Parent executeOpts;
  @FXML private ExecuteOptsController executeOptsController;
  @FXML private Parent codegenOpts;
  @FXML private CodegenOptsController codegenOptsController;
  @FXML private Parent optimizationOpts;
  @FXML private OptimizationOptsController optimizationOptsController;
  @FXML private Parent developerOpts;
  @FXML private DeveloperOptsController developerOptsController;
  @FXML private ChooseUserController chooseUserController;
  @FXML public RunningProjectController runningProjectController;

  // Tabs
  @FXML private Tab developerOptions;
  @FXML private Tab optimizationTab;
  @FXML private Tab codeGenerationOptions;
  @FXML private Tab codeExecuteOptions;
  @FXML private Tab debuggingOptions;
  @FXML private Tab linkingOptions;
  @FXML private Tab compilerOptions;

  // Check Menu Items
  @FXML private CheckMenuItem compilingCheck;
  @FXML private CheckMenuItem linkingCheck;
  @FXML private CheckMenuItem executingCheck;
  @FXML private CheckMenuItem debuggingCheck;
  @FXML private CheckMenuItem codeGenerationCheck;
  @FXML private CheckMenuItem codeOptimizationCheck;
  @FXML private CheckMenuItem developerCheck;

  /** THe constructor of the class */
  public MainController() {}

  /** Function to initialize the Main Controller */
  @FXML
  public void initialize() {
    System.out.println("maincontroller initialized");
    this.smartModel = new SmartModel();
    this.commandExecute = new CommandExecute(this);
    this.projects = new LinkedList<TabProjectPane>();
    this.textAreaResult = new TextArea();
    this.textAreaResult.setEditable(false);
    this.textResult.setContent(textAreaResult);
    this.projectsPane
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ChangeListener) new ChangeListenerTabs(this));
  }

  /** Function that closes the application when quit is pressed */
  @FXML
  void close() {
    //    optimizationPanelController.getOptimizationFlags();
    //    debugPanelController.getDebugFlags();
    System.exit(0);
  }
  /**
   * It creates the string with the different selected flags from the radio buttons and check boxes
   *
   * @return flags
   */
  private String buildArgStr() {
    List<String> args = new ArrayList<String>();
    String argsStr = "";
    // "Simple-Opt" Collection
    Set<Node> m = Main.getScene().getRoot().lookupAll(".simple-opt");
    for (Node mm : m) {
      // Checkbox
      boolean isCheckBoxSelected = mm instanceof CheckBox && ((CheckBox) mm).isSelected();
      if (isCheckBoxSelected) {
        args.add(((CheckBox) mm).getText());
        argsStr = argsStr + ((CheckBox) mm).getText() + " ";
      }
      // ToggleButton (includes RadioButton)
      boolean isToggleButtonSelected =
          mm instanceof ToggleButton && ((ToggleButton) mm).isSelected();
      if (isToggleButtonSelected) {
        args.add(((ToggleButton) mm).getText());
        argsStr = argsStr + ((ToggleButton) mm).getText() + " ";
      }
    }
    return argsStr;
  }

  /** Function to build a project */
  @FXML
  private void buildProject() {
    if (projectsPane.getTabs().size() > 0) {
      SingleSelectionModel<Tab> selectionModel = projectsPane.getSelectionModel();
      Project prCompile = smartModel.getProject(selectionModel.getSelectedItem().getText());
      this.panelCompilingOptions.getSelectionModel().select(this.textResult);
      String argStr = buildArgStr();
      prCompile.setAllFlags(argStr);
      String libs = linkingOptsController.buildLibs();
      try {
        String[] result = this.commandExecute.buildProject(prCompile, argStr, libs);
        this.textAreaResult.setText(
            this.textAreaResult.getText()
                + "\n"
                + "The executed command was:\n"
                + result[0]
                + "\n"
                + "The operation was:\n"
                + result[1]
                + "The result was:\n"
                + result[2]);
      } catch (Exception e) {
        System.out.println("Error in execution");
      }
    } else {
      Alert alertLibrary = new Alert(AlertType.ERROR);
      alertLibrary.setTitle("Error Dialog");
      alertLibrary.setContentText("Please create a project.");
      alertLibrary.showAndWait();
    }
  }

  /** Function to close a project from the Tab Pane */
  @FXML
  private void closeProject() {
    if (this.projectsPane.getTabs().size() > 0) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm this action.");
      alert.setContentText("Are you sure you want to delete the project?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        TabProjectPane tab =
            (TabProjectPane) this.projectsPane.getSelectionModel().getSelectedItem();
        this.smartModel.deleteProject(tab.getText());
        this.projects.remove(tab);
        this.projectsPane.getTabs().remove(tab);
      }
    } else {
      Alert alertLibrary = new Alert(AlertType.ERROR);
      alertLibrary.setTitle("Error Dialog");
      alertLibrary.setHeaderText("Can't close project");
      alertLibrary.setContentText("Please create or open a project first.");
      alertLibrary.showAndWait();
    }
  }

  /** Function that shows the help panel */
  @FXML
  void onHelpAction() {
    this.helpLayout = new StackPane();
    VBox helpPane = null;
    try {
      helpPane = FXMLLoader.load(getClass().getResource("/application/views/help.fxml"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    this.helpLayout.getChildren().add(helpPane);

    Scene secondScene = new Scene(this.helpLayout);

    // New window (Stage)
    Stage newWindow = new Stage();
    newWindow.setResizable(false);
    newWindow.setMinHeight(400);
    newWindow.setMinWidth(600);
    newWindow.initModality(Modality.WINDOW_MODAL);
    newWindow.initOwner(Main.getStage());
    newWindow.setTitle("Help Options");
    newWindow.setScene(secondScene);

    // Set position of second window, related to primary window.
    newWindow.setX(Main.getStage().getX() + 300);
    newWindow.setY(Main.getStage().getY() + 100);

    newWindow.show();
  }

  /** Function that shows the New Project panel */
  @FXML
  private void onNewProjectAction() {

    this.secondaryLayout = new StackPane();

    // Project Directory
    Label labelProjectDirectory = new Label("Project Directory:");

    inputProjectDirectory = new TextField();
    inputProjectDirectory.setDisable(true);

    Button submit = new Button("Select");

    DirectoryChooser directoryChooser = new DirectoryChooser();

    EventHandler<ActionEvent> event =
        new EventHandler<ActionEvent>() {

          public void handle(ActionEvent e) {

            // get the file selected
            File selectedDirectory = directoryChooser.showDialog(Main.getStage());

            if (selectedDirectory != null) {
              inputProjectDirectory.setText(selectedDirectory.getAbsolutePath());
            }
          }
        };

    submit.setOnAction(event);

    HBox hbProjectDirectory = new HBox();
    hbProjectDirectory.getChildren().addAll(labelProjectDirectory, inputProjectDirectory, submit);
    hbProjectDirectory.setSpacing(10);

    // Project Name
    Label labelProjectName = new Label("Project Name:");
    inputProjectName = new RulesTextField();
    inputProjectName.setMinSize(150, 25);
    HBox hbProjectName = new HBox();
    hbProjectName.getChildren().addAll(labelProjectName, inputProjectName);
    hbProjectName.setSpacing(10);

    // Files
    HBox hbFileOptions = this.createFileOptions("");

    // Create and Cancel Button
    Button createButton = new Button("Create");
    Button cancelButton = new Button("Cancel");

    cancelButton.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
          }
        });

    createButton.setOnAction(this::createProject);

    HBox hbFinalButtons = new HBox();
    hbFinalButtons.getChildren().addAll(createButton, cancelButton);
    hbFinalButtons.setAlignment(Pos.BASELINE_CENTER);
    hbFinalButtons.setSpacing(10);

    VBox newProject = new VBox();
    newProject
        .getChildren()
        .addAll(hbProjectDirectory, hbProjectName, hbFileOptions, textAreaFiles, hbFinalButtons);
    newProject.setPadding(new Insets(10, 50, 50, 50));
    newProject.setSpacing(10);

    // Adding elements
    this.secondaryLayout.getChildren().add(newProject);
    Scene secondScene = new Scene(this.secondaryLayout, 500, 400);

    // New window (Stage)
    Stage newWindow = new Stage();
    newWindow.setMinHeight(400);
    newWindow.setMinWidth(400);
    newWindow.initModality(Modality.WINDOW_MODAL);
    newWindow.initOwner(Main.getStage());
    newWindow.setTitle("New Project");
    newWindow.setScene(secondScene);

    // Set position of second window, related to primary window.
    newWindow.setX(Main.getStage().getX() + 300);
    newWindow.setY(Main.getStage().getY() + 100);

    newWindow.show();
  }

  /**
   * This function verifies the data inserted by the user to create a new project, it gives an error
   * if the mandatory fields are not provided.
   *
   * @param event create project event
   */
  @FXML
  public void createProject(ActionEvent event) {

    LinkedList<String> sourceFiles = this.getSourceFiles(this.textAreaFiles.getText());
    LinkedList<String> objectFiles = this.getObjectFiles(this.textAreaFiles.getText());
    LinkedList<String> libraries = this.createLibraryVariables();
    if (sourceFiles != null && objectFiles != null && libraries != null) {
      if (this.inputProjectDirectory.getText().equals("")
          || this.inputProjectName.getText().equals("")
          || this.textAreaFiles.getText().equals("")) {
        Alert alertLibrary = new Alert(AlertType.ERROR);
        alertLibrary.setTitle("Error Dialog");
        alertLibrary.setHeaderText("The project was not created");
        alertLibrary.setContentText("Please provide the mandatory fields.");
        alertLibrary.showAndWait();

      } else {
        if (this.smartModel.createProject(
                this.inputProjectName.getText(),
                sourceFiles,
                objectFiles,
                libraries,
                this.inputProjectDirectory.getText()
                    + File.separator
                    + this.inputProjectName.getText())
            == -1) {
          Alert alertLibrary = new Alert(AlertType.ERROR);
          alertLibrary.setTitle("Error Dialog");
          alertLibrary.setHeaderText("The project was not created");
          alertLibrary.setContentText(
              "Please change the project name, there is a project with that name");
          alertLibrary.showAndWait();
        } else {
          // System.out.println();
          Stage stage = (Stage) this.inputProjectName.getScene().getWindow();
          if (this.createProjectDirectory() == 0) {
            this.createProjectOnView(this.smartModel.getProject(this.inputProjectName.getText()));
            stage.close();
          }
        }
      }
    }
  }

  /**
   * The function loads the stored flags for the project when you exchange between two different
   * projects
   *
   * @param name of the project
   */
  public void loadSelectedElements(String name) {
    Project pr = this.smartModel.getProject(name);
    List<String> listFlags = null;

    if (pr.allFlags() == null || pr.allFlags().equals("")) {
      listFlags = new LinkedList<String>();
    } else {
      String[] options = pr.allFlags().split(" ");
      listFlags = Arrays.asList(options);
    }

    Set<Node> m = Main.getScene().getRoot().lookupAll(".simple-opt");
    for (Node mm : m) {
      if (mm instanceof CheckBox) {
        if (listFlags.contains(((CheckBox) mm).getText())) {
          ((CheckBox) mm).setSelected(true);
        } else {
          ((CheckBox) mm).setSelected(false);
        }
      } else if (mm instanceof ToggleButton) {
        if (listFlags.contains(((ToggleButton) mm).getText())) {
          ((ToggleButton) mm).setSelected(true);
        } else {
          ((ToggleButton) mm).setSelected(false);
        }
      }
    }
  }

  /**
   * This creates the HBox with the files text area, it verifies the extension of the files added by
   * the user.
   *
   * @param textFile file added
   * @return HBox to the new project and edit project view
   */
  public HBox createFileOptions(String textFile) {
    LinkedList<String> extensions =
        new LinkedList<String>(Arrays.asList("c", "o", "C", "cpp", "CPP", "c++", "cp", "cxx"));
    Label labelFiles = new Label("Dependency Files");
    Button addFile = new Button("+");
    Button deleteFile = new Button("-");

    textAreaFiles = new TextArea();
    textAreaFiles.setText(textFile);
    textAreaFiles.setEditable(false);
    if (textAreaFiles.getText().contentEquals("")) deleteFile.setDisable(true);

    FileChooser selectFile = new FileChooser();
    EventHandler<ActionEvent> eventAddFiles =
        new EventHandler<ActionEvent>() {

          public void handle(ActionEvent e) {

            // get the file selected
            File selectedFile = selectFile.showOpenDialog(Main.getStage());

            if (selectedFile != null) {
              String selectedFilePath = selectedFile.getAbsolutePath();

              String extension =
                  selectedFilePath.substring(
                      selectedFilePath.lastIndexOf(".") + 1, selectedFilePath.length());

              if ((selectedFilePath.lastIndexOf(".") + 1) != 0
                  && (extensions.contains(extension))) {
                textAreaFiles.setText(
                    textAreaFiles.getText() + selectedFile.getAbsolutePath() + "\n");
                deleteFile.setDisable(false);
              } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Bad File extension");
                alert.setContentText("Please select only files with gcc valid extensions.");
                alert.showAndWait();
              }
            }
          }
        };

    addFile.setOnAction(eventAddFiles);

    deleteFile.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            String textAreaModify = textAreaFiles.getText();

            if (!textAreaModify.contentEquals(""))
              textAreaModify = textAreaModify.substring(0, (textAreaModify.lastIndexOf("\n")));
            textAreaModify = textAreaModify.substring(0, (textAreaModify.lastIndexOf("\n") + 1));
            textAreaFiles.setText(textAreaModify);
            if (textAreaModify.equals("")) deleteFile.setDisable(true);
          }
        });

    HBox hbFileOptions = new HBox();
    hbFileOptions.getChildren().addAll(labelFiles, addFile, deleteFile);
    hbFileOptions.setSpacing(10);
    return hbFileOptions;
  }

  /**
   * This calls the model to update the information of the project.
   *
   * @param name The name of the project
   * @param files The new files
   */
  public void updateProject(String name, String files) {

    LinkedList<String> sourceFiles = this.getSourceFiles(this.textAreaFiles.getText());
    LinkedList<String> objectFiles = this.getObjectFiles(this.textAreaFiles.getText());

    if (sourceFiles != null && objectFiles != null) {
      if (this.textAreaFiles.getText().equals("")) {
        Alert alertLibrary = new Alert(AlertType.ERROR);
        alertLibrary.setTitle("Error Dialog");
        alertLibrary.setHeaderText("The project was not created");
        alertLibrary.setContentText("Please provide the mandatory fields.");
        alertLibrary.showAndWait();

      } else {
        Project pr = this.smartModel.updateProject(name, sourceFiles, objectFiles);
        ScrollPane scroll = this.setProjectInfo(pr);
        for (TabProjectPane tab : this.projects) {
          if (tab.getText().equals(pr.getName())) {
            tab.setScroll(scroll);
            tab.setContent(scroll);
          }
        }

        Stage stage = (Stage) this.textAreaFiles.getScene().getWindow();
        stage.close();
      }
    }
  }

  /** Save the list of projects */
  @FXML
  public void saveListProject() {
    FileOutputStream fileOutputStream;
    try {

      fileOutputStream =
          new FileOutputStream(
              new File("saveProject" + File.separator + "listOfProjects.txt").getAbsolutePath());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(this.smartModel.getListProjects());
      objectOutputStream.flush();
      objectOutputStream.close();
    } catch (Exception e) {
      System.out.println("Problems saving project");
    }
  }

  /**
   * Update the files of a project in the model
   *
   * @param name The name of the project
   */
  public void updateProjectArgs(String name) {
    if (this.projectsPane.getTabs().size() > 0) {
      String projectFlags = this.buildArgStr();
      Project pr = this.smartModel.getProject(name);
      if (projectFlags != null) pr.setAllFlags(projectFlags);
      else pr.setAllFlags("");
    }
  }

  /** Function to save a project in the users computer */
  @FXML
  public void onSaveProjectAction() {
    if (this.projectsPane.getTabs().size() > 0) {
      FileOutputStream fileOutputStream;
      String name = this.projectsPane.getSelectionModel().getSelectedItem().getText();
      Project pr = this.smartModel.getProject(name);
      this.updateProjectArgs(pr.getName());
      try {
        String directory = pr.getProjectLocation() + File.separator + pr.getName() + ".smartgcc";
        // System.out.println(directory);
        fileOutputStream = new FileOutputStream(new File(directory).getAbsolutePath());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(pr);
        objectOutputStream.flush();
        objectOutputStream.close();
        Alert alertLibrary = new Alert(AlertType.INFORMATION);
        alertLibrary.setTitle("Save Project");
        alertLibrary.setContentText("The project was saved.");
        alertLibrary.showAndWait();
      } catch (Exception e) {
        System.out.println("Problems saving project");
      }
    } else {
      Alert alertLibrary = new Alert(AlertType.ERROR);
      alertLibrary.setTitle("Error Dialog");
      alertLibrary.setHeaderText("Please create a Project");
      alertLibrary.setContentText("Please create a project first.");
      alertLibrary.showAndWait();
    }
  }

  /** Loads the list of projects in the application */
  public void loadListOfProject() {
    FileInputStream fileInputStream;
    LinkedList<Project> listProject = null;
    try {
      fileInputStream =
          new FileInputStream(
              new File("saveProject" + File.separator + "listOfProjects.txt").getAbsolutePath());
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      listProject = (LinkedList<Project>) objectInputStream.readObject();

      objectInputStream.close();
    } catch (Exception e) {
      System.out.println("No file found");
    } finally {
      if (listProject == null) this.smartModel.setListProjects(new LinkedList<Project>());
      else {
        this.smartModel.setListProjects(listProject);
        for (Project pr : listProject) {
          this.createProjectOnView(pr);
        }
      }
    }
  }

  /**
   * Creates the edit Project Panel
   *
   * @param pr the project
   */
  private void generateEditProjectPanel(Project pr) {

    this.secondaryLayout = new StackPane();

    // Project Directory
    Label labelProjectDirectory = new Label("Project Directory:");

    inputProjectDirectory = new TextField();
    inputProjectDirectory.setText(pr.getProjectLocation());
    inputProjectDirectory.setDisable(true);

    HBox hbProjectDirectory = new HBox();
    hbProjectDirectory.getChildren().addAll(labelProjectDirectory, inputProjectDirectory);
    hbProjectDirectory.setSpacing(10);

    // Project Name
    Label labelProjectName = new Label("Project Name:");
    inputProjectName = new RulesTextField();
    inputProjectName.setMinSize(150, 25);
    inputProjectName.setText(pr.getName());
    inputProjectName.setDisable(true);
    HBox hbProjectName = new HBox();
    hbProjectName.getChildren().addAll(labelProjectName, inputProjectName);
    hbProjectName.setSpacing(10);

    // Files
    String textFiles =
        CommandExecute.generateStringFromList(pr.getSourceFiles())
            + CommandExecute.generateStringFromList(pr.getObjectFiles());
    HBox hbFileOptions = this.createFileOptions(textFiles);

    // Create and Cancel Button
    Button createButton = new Button("Update");
    Button cancelButton = new Button("Cancel");

    cancelButton.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent e) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
          }
        });

    createButton.setOnAction(e -> updateProject(pr.getName(), textAreaFiles.getText()));

    HBox hbFinalButtons = new HBox();
    hbFinalButtons.getChildren().addAll(createButton, cancelButton);
    hbFinalButtons.setAlignment(Pos.BASELINE_CENTER);
    hbFinalButtons.setPadding(new Insets(10, 0, 0, 0));
    hbFinalButtons.setSpacing(10);

    VBox newProject = new VBox();
    newProject
        .getChildren()
        .addAll(hbProjectDirectory, hbProjectName, hbFileOptions, textAreaFiles, hbFinalButtons);
    newProject.setPadding(new Insets(10, 50, 50, 50));
    newProject.setSpacing(10);

    // Adding elements
    this.secondaryLayout.getChildren().add(newProject);
    Scene secondScene = new Scene(this.secondaryLayout, 500, 400);

    // New window (Stage)
    Stage newWindow = new Stage();
    newWindow.setMinHeight(400);
    newWindow.setMinWidth(400);
    newWindow.initModality(Modality.WINDOW_MODAL);
    newWindow.initOwner(Main.getStage());
    newWindow.setTitle("Update Project");
    newWindow.setScene(secondScene);

    // Set position of second window, related to primary window.
    newWindow.setX(Main.getStage().getX() + 300);
    newWindow.setY(Main.getStage().getY() + 100);

    newWindow.show();
  }

  /**
   * Shows the project information
   *
   * @param project object to show
   * @return A scroll panel with the project information
   */
  public ScrollPane setProjectInfo(Project project) {
    ScrollPane scrollPane = new ScrollPane();

    Button update = new Button("Edit");
    update.setOnAction(e -> generateEditProjectPanel(project));
    HBox buttonBox = new HBox(update);
    buttonBox.setPadding(new Insets(30, 0, 0, 0));

    String styleBold = "-fx-font-weight: bold";

    Label projectName = new Label("Project Name:");
    projectName.setPadding(new Insets(20, 0, 0, 0));
    Label directoryName = new Label("Project Directory:");
    directoryName.setPadding(new Insets(20, 0, 0, 0));
    Label projectFiles = new Label("Project Files:");
    projectFiles.setPadding(new Insets(20, 0, 0, 0));

    projectName.setStyle(styleBold);
    directoryName.setStyle(styleBold);
    projectFiles.setStyle(styleBold);

    VBox leftControl = new VBox();
    leftControl
        .getChildren()
        .addAll(
            projectName,
            new Label(project.getName()),
            directoryName,
            new Label(project.getProjectLocation()),
            projectFiles,
            new Label(
                CommandExecute.generateStringFromList(project.getSourceFiles())
                    + CommandExecute.generateStringFromList((project.getObjectFiles()))),
            buttonBox);

    scrollPane.setContent(leftControl);
    return scrollPane;
  }

  /**
   * Generates the tab of the project and calls the function that creates the scroll panel with the
   * project information
   *
   * @param project the project to create on the view
   */
  public void createProjectOnView(Project project) {

    TabProjectPane tab = new TabProjectPane(project.getName());
    ScrollPane scrollPane = this.setProjectInfo(project);
    scrollPane.setMinSize(300, 300);
    tab.setContent(scrollPane);
    tab.setScroll(scrollPane);
    tab.setOnCloseRequest(
        e -> {
          Alert alert = new Alert(AlertType.CONFIRMATION);
          alert.setTitle("Confirmation Dialog");
          alert.setHeaderText("Please confirm this action.");
          alert.setContentText("Are you sure you want to delete the project?");

          Optional<ButtonType> result = alert.showAndWait();
          if (result.get() == ButtonType.OK) {
            String name = tab.getText();
            System.out.println(name);
            this.smartModel.deleteProject(name);
            this.projects.remove(tab);
          } else {
            e.consume();
          }
        });

    this.loadSelectedElements(project.getName());
    this.projects.add(tab);
    this.projectsPane.getTabs().add(tab);
    this.projectsPane.getSelectionModel().select(tab);
  }

  /**
   * Create the directory for the project in the user's computer
   *
   * @return 0 if no error
   */
  public int createProjectDirectory() {
    File file =
        new File(
            this.inputProjectDirectory.getText()
                + File.separator
                + this.inputProjectName.getText());

    if (!file.exists()) {
      if (file.mkdir()) {
        System.out.println("Directory is created!");
      }
    } else {
      Alert alertLibrary = new Alert(AlertType.ERROR);
      alertLibrary.setTitle("Error Dialog");
      alertLibrary.setHeaderText("The project was not created");
      alertLibrary.setContentText(
          "Please change the project name, there is a directory with the project name you introduce.");
      alertLibrary.showAndWait();
      this.smartModel.deleteProject(this.inputProjectName.getText());
      return -1;
    }

    return 0;
  }

  /**
   * Produces a list of the source files
   *
   * @param files the files provided by the user in a string
   * @return the list of source files
   */
  public LinkedList<String> getSourceFiles(String files) {
    String filesArray[] = files.split("\n");
    LinkedList<String> sourceFiles = new LinkedList<String>();

    for (int i = 0; i < filesArray.length; i++) {
      if ((!filesArray[i].equals(""))
          && !(filesArray[i]
              .substring(filesArray[i].lastIndexOf(".") + 1, filesArray[i].length())
              .contentEquals("o"))) {
        if (sourceFiles.contains(filesArray[i])) {
          Alert alertFileRep = new Alert(AlertType.ERROR);
          alertFileRep.setTitle("Error Dialog");
          alertFileRep.setHeaderText("The project was not created");
          alertFileRep.setContentText("You repeated files.");
          alertFileRep.showAndWait();
          return null;
        } else {
          sourceFiles.add(filesArray[i]);
        }
      }
    }

    return sourceFiles;
  }

  /**
   * Produces a list of object files
   *
   * @param files The files in a String
   * @return a list of Strings with the files
   */
  public LinkedList<String> getObjectFiles(String files) {
    String filesArray[] = files.split("\n");
    LinkedList<String> objectFiles = new LinkedList<String>();

    for (int i = 0; i < filesArray.length; i++) {
      if ((!filesArray[i].equals(""))
          && (filesArray[i]
              .substring(filesArray[i].lastIndexOf(".") + 1, filesArray[i].length())
              .contentEquals("o"))) {
        if (objectFiles.contains(filesArray[i])) {
          Alert alertFileRep = new Alert(AlertType.ERROR);
          alertFileRep.setTitle("Error Dialog");
          alertFileRep.setHeaderText("The project was not created");
          alertFileRep.setContentText("You repeated files.");
          alertFileRep.showAndWait();
          return null;
        } else {
          objectFiles.add(filesArray[i]);
        }
      }
    }

    return objectFiles;
  }

  /**
   * Creates a linked list every time it is necessary
   *
   * @return linked list
   */
  public LinkedList<String> createLibraryVariables() {
    return new LinkedList<String>();
  }

  /** Special action */
  @FXML
  private void onSpecialAction() {
    Node n = Main.getScene().lookup(".linking-libraries");
    Set<Node> m = Main.getScene().getRoot().lookupAll(".simple-opt");
    List<String> args = new ArrayList<String>();
    for (Node mm : m) {
      if (mm instanceof CheckBox && ((CheckBox) mm).isSelected()) {
        args.add(((CheckBox) mm).getText());
        System.out.println(((CheckBox) mm).getText());
      }
    }
  }

  /**
   * Executed when the user presses the open project button
   *
   * @return 0 if success
   */
  @FXML
  private int onOpenProjectAction() {
    FileChooser fileChooser = new FileChooser();
    File selectedFile = fileChooser.showOpenDialog(Main.getStage());
    int i = 0;

    if (selectedFile != null) {

      String extension =
          selectedFile
              .getAbsolutePath()
              .substring(selectedFile.getAbsolutePath().lastIndexOf(File.separator) + 1);

      if (extension.split("\\.").length < 2
          || !((extension.split("\\.")[1]).contentEquals("smartgcc"))) {
        Alert alertLibrary = new Alert(AlertType.ERROR);
        alertLibrary.setTitle("Error Dialog");
        alertLibrary.setContentText("The file selected doesn't have an appropiate extension");
        alertLibrary.showAndWait();
        return -1;
      }

      String name =
          selectedFile
              .getAbsolutePath()
              .substring(selectedFile.getAbsolutePath().lastIndexOf(File.separator) + 1)
              .split(".smartgcc")[0];

      for (Tab tab : this.projectsPane.getTabs()) {
        if (tab.getText().equals(name)) {
          i = 1;
        }
      }

      if (i == 1) {
        Alert alertLibrary = new Alert(AlertType.ERROR);
        alertLibrary.setTitle("Error Dialog");
        alertLibrary.setContentText("The project is already open.");
        alertLibrary.showAndWait();
        return -1;
      }

      Project pr = null;
      FileInputStream fileInputStream;
      try {
        fileInputStream =
            new FileInputStream(new File(selectedFile.getAbsolutePath()).getAbsolutePath());
        System.out.println(selectedFile.getAbsolutePath());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        pr = (Project) objectInputStream.readObject();

        if (this.smartModel.addProject(pr) == -1) {
          Alert alertLibrary = new Alert(AlertType.ERROR);
          alertLibrary.setTitle("Error Dialog");
          alertLibrary.setContentText("The project was not saved. It can not be open.");
          alertLibrary.showAndWait();
          return -1;
        } else {
          this.createProjectOnView(pr);
        }

        objectInputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  /**
   * All options panel handler
   *
   * @param event manages a click on the all options panel
   */
  @FXML
  void handleAllOptions(ActionEvent event) {

    CheckMenuItem sourceButton = (CheckMenuItem) event.getSource();
    String compilationPanel = sourceButton.getText();

    switch (compilationPanel) {
      case "Code Generation Options":
        if (containTab(this.codeGenerationOptions.getText()) == -1 && sourceButton.isSelected()) {
          Main.getProfile().add("code-generation");
        }
        if (!sourceButton.isSelected() && containTab(this.codeGenerationOptions.getText()) == 0) {
          Main.getProfile().remove("code-generation");
        }
        break;
      case "Code Optimization Options":
        if (containTab(this.optimizationTab.getText()) == -1 && sourceButton.isSelected()) {
          Main.getProfile().add("code-optimization");
        }
        if (!sourceButton.isSelected() && containTab(this.optimizationTab.getText()) == 0) {
          Main.getProfile().remove("code-optimization");
        }
        break;
      case "Developer Options":
        if (containTab(this.developerOptions.getText()) == -1 && sourceButton.isSelected()) {
          Main.getProfile().add("developer");
        }
        if (!sourceButton.isSelected() && containTab(this.developerOptions.getText()) == 0) {
          Main.getProfile().remove("developer");
        }
        break;
    }
    generatePanels();
  }

  /**
   * Verifies if the tab is in the compiling options pane
   *
   * @param name name of the tab
   * @return 0 if it is
   */
  public int containTab(String name) {

    for (Tab tab : this.panelCompilingOptions.getTabs()) {
      if (tab.getText().contentEquals(name)) return 0;
    }
    return -1;
  }

  /** This is called to execute a project. It is a function under development. */
  @FXML
  public void executeProject() {

    if (projectsPane.getTabs().size() > 0) {
      SingleSelectionModel<Tab> selectionModel = projectsPane.getSelectionModel();
      Project prRun = smartModel.getProject(selectionModel.getSelectedItem().getText());
      this.commandExecute.setInputRun(this.runningProjectController.inputExecute.getText());
      try {
        String[] results = this.commandExecute.runProgram(prRun);
        this.runningProjectController.resultExecute.setText(
            this.runningProjectController.resultExecute.getText()
                + "Errors:\n"
                + results[0]
                + "\n"
                + "Results:\n"
                + results[1]
                + "\n");
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /** It shows all the initial panels when a type of user is selected */
  public void generatePanels() {
    Set<String> profile = Main.getProfile();
    this.panelCompilingOptions.getTabs().remove(this.developerOptions);
    this.panelCompilingOptions.getTabs().remove(this.codeGenerationOptions);
    this.panelCompilingOptions.getTabs().remove(this.optimizationTab);

    if (!profile.contains("code-generation")) {
      this.codeGenerationCheck.setSelected(false);
    } else {
      this.codeGenerationCheck.setSelected(true);
      this.panelCompilingOptions.getTabs().add(this.codeGenerationOptions);
    }
    if (!profile.contains("code-optimization")) {
      this.codeOptimizationCheck.setSelected(false);
    } else {
      this.codeOptimizationCheck.setSelected(true);
      this.panelCompilingOptions.getTabs().add(this.optimizationTab);
    }
    if (!profile.contains("developer")) {
      this.developerCheck.setSelected(false);
    } else {
      this.developerCheck.setSelected(true);
      this.panelCompilingOptions.getTabs().add(this.developerOptions);
    }
    Main.saveProfile();
  }

  /**
   * Stub for cut action
   *
   * @param event
   */
  @FXML
  private void onCutAction(ActionEvent event) {}

  /**
   * Stub for copy action
   *
   * @param event
   */
  @FXML
  private void onCopyAction(ActionEvent event) {}

  /**
   * Stub for paste action
   *
   * @param event
   */
  @FXML
  private void onPasteAction(ActionEvent event) {}

  /**
   * Shows the initial profile options.
   *
   * @throws IOException
   */
  @FXML
  private void onProfileResetAction() throws IOException {
    Main.saveWorkspace();
    Main.setRoot("/application/views/ChooseUser");
  }
}
