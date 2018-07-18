import customExceptions.NotFlashable;
/**
 * Model for calculating the K values.
 * Child classes: IdealModel, PRModel, and WilsonSRKModel.
 */
public abstract class VLEModel
{
    public abstract double[] calculateKi(FlashTank flashTank) throws NotFlashable;
}