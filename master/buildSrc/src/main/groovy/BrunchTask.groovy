import org.gradle.api.tasks.Exec

class BrunchTask extends Exec {
    private String brunchExecutable =  "brunch"
    private String switches = "--no-color"
 
    String gruntArgs = "" 
 
    public BrunchTask() {
        super()
        this.setExecutable(gruntExecutable)
    }
 
    public void setBrunchArgs(String brunchArgs) {
        this.args = "$switches $brunchArgs".trim().split(" ") as List
    }
}
