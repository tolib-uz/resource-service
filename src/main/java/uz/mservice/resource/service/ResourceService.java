package uz.mservice.resource.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import uz.mservice.resource.exception.ResourceValidationException;
import uz.mservice.resource.model.Resource;
import uz.mservice.resource.model.SongRequest;
import uz.mservice.resource.repository.ResourceRepository;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final RestTemplate restTemplate;

    public ResourceService(ResourceRepository resourceRepository, RestTemplate restTemplate) {
        this.resourceRepository = resourceRepository;
        this.restTemplate = restTemplate;
    }

    private Metadata extractMetadata(byte[] audioFile) {
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        Parser mp3Parser = new Mp3Parser();
        try(InputStream stream =new ByteArrayInputStream(audioFile)){
            mp3Parser.parse(stream, handler,metadata, new ParseContext());
            return metadata;
        }catch (IOException | TikaException | SAXException ex){
            throw new ResourceValidationException((ex.getMessage()));
        }
    }

    public Resource saveResource(byte[] audioFile){
        Resource resource = new Resource();
        resource.setFile(audioFile);
        Resource savedResource = resourceRepository.save(resource);

        Metadata metadata = extractMetadata(audioFile);
        SongRequest songRequest = new SongRequest();

        songRequest.setName(metadata.get("name"));
        songRequest.setArtist(metadata.get("xmpDM:artist"));
        songRequest.setAlbum(metadata.get("xmpDM:album"));
        songRequest.setLength(metadata.get("xmpDM:duration"));
        songRequest.setYear(metadata.get("xmpDM:releaseDate"));
        songRequest.setResourceId(savedResource.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SongRequest> request = new HttpEntity<>(songRequest, headers);
        String uri = "/songs";
        restTemplate.postForObject(uri,request, SongRequest.class);
        System.out.println(restTemplate.getUriTemplateHandler());

        return savedResource;



    }

    public void deleteResources(String ids){
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        resourceRepository.deleteAllByIdInBatch(idList);
    }

    public Resource listResources(Long id){
        Optional<Resource> resource = resourceRepository.findById(id);
        if(!resource.isPresent()){
            throw new ResourceValidationException("The resource with the specified id does not exist.");
        }
        return resource.get();

    }
}
