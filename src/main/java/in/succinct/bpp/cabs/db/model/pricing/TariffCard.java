package in.succinct.bpp.cabs.db.model.pricing;

import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;

public interface TariffCard extends Model {
    public Long getDeploymentPurposeId();
    public void setDeploymentPurposeId(Long id);
    public DeploymentPurpose getDeploymentPurpose();

    public String getTag();
    public void setTag(String tag);

    public Integer getFromKms();
    public void setFromKms(Integer fromKms);

    public Integer getToKms();
    public void setToKms(Integer toKms);

    public Double getFixedPrice();
    public void setFixedPrice(Double fixedPrice);

    public Double getPricePerKm();
    public void setPricePerKm(Double pricePerKm);

    public Double getPricePerHour();
    public void setPricePerHour(Double pricePerHour);


}