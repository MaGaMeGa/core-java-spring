package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.arrowhead.common.database.entity.ChoreographerStepDetail;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChoreographerStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3665162578177568728L;

	private long id;
    private String name;
    private List<ChoreographerStepDetail> stepDetails;
    private String metadata;
    private String parameters;
    private int quantity;
    private List<ChoreographerNextStepResponseDTO> nextSteps;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepResponseDTO(final long id, final String name, final List<ChoreographerStepDetail> stepDetails, final String parameters,
                                        final List<ChoreographerNextStepResponseDTO> nextSteps, final int quantity, final String createdAt, final String updatedAt) {
        this.id = id;
        this.name = name;
        this.stepDetails = stepDetails;
        this.parameters = parameters;
        this.nextSteps = nextSteps;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
    public List<ChoreographerStepDetail> getStepDetails() { return stepDetails; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }
    public List<ChoreographerNextStepResponseDTO> getNextSteps() { return nextSteps; }
    public int getQuantity() { return quantity; }
    public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setStepDetails(List<ChoreographerStepDetail> stepDetails) { this.stepDetails = stepDetails; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public void setNextSteps(final List<ChoreographerNextStepResponseDTO> nextSteps) { this.nextSteps = nextSteps; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}