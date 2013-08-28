#include <stdlib.h>
#include <stdio.h>
#include "hdf.h"
#include "mfhdf.h"

#define MAX_SDSNAME_LEN	128
#define MAX_VARNAME_LEN 16
#define MAX_DIM_LEN 2000
#define FILENAME_LEN 128

struct varnames {
	varnames(int s) {
		size = s;
		names = new char*[size];
	}
	~varnames() {
		delete [] names;
	}
	void assign(char* namearray[]) {
		for (int i=0; i<size; ++i) {
			names[i] = namearray[i];
		}
	}
	
	char **names;
	int size;
};

int readdataset(int sd_id, char *sds_name,
				int *start, int *stride, int *edge,
				VOIDP sds_array) {

	// retrieve data set's index
	int sds_index = SDnametoindex(sd_id, sds_name);
	if (sds_index == FAIL) {
		printf("Failed to retrieve %s's index.\n", sds_name);
		return FAIL;
	}
	
	// retrieve data set's ID
	int sds_id = SDselect(sd_id, sds_index);
	if (sds_id == FAIL) {
		printf("Failed to retrieve %s's ID.\n", sds_name);
		return FAIL;
	}
	
	// retrieve data set
	int ret = SDreaddata(sds_id, start, stride, edge, sds_array);
	if (ret == FAIL) {
		printf("Failed to retrieve %s's data set.\n", sds_name);
		return FAIL;
	}
	
	SDendaccess(sds_id);
}


int save2file_dim2_int16(FILE *fp,
						varnames *names,
						int16 *chunks,
						int yidx, int xidx,
						int ystart, int xstart,
						int ylen, int xlen) {
	/*
	// generate file name
	char fpname[FILENAME_LEN];
	memset(fpname, 0, FILENAME_LEN);
	sprintf(fpname, "2dimsplit_%d_%d.bsc", xidx, yidx);
	FILE *fp = fopen(fpname, "w");
	if (!fp) {
		printf("Failed to open file to write data chunk.\n");
		return -1;
	}
	*/
	
	for (int j=0; j<ylen; ++j) {
		for (int i=0; i<xlen; ++i) {
			fprintf(fp, "%d %d ", xstart+i, ystart+j);
			for (int num=0; num<names->size; ++num) {
				fprintf(fp, "%d ", *(chunks+ylen*xlen*num+xlen*j+i));
			}
			fprintf(fp, "\n");
		}
	}
	// fclose(fp);
}

int save2file_dim3_int16(FILE *fp,
						varnames *names,
						int16 *chunks,
						int zidx, int yidx, int xidx,
						int ystart, int xstart,
						int ylen, int xlen) {
	/*
	// generate file name
	char fpname[FILENAME_LEN];
	memset(fpname, 0, FILENAME_LEN);
	sprintf(fpname, "3dimsplit_level%d_%d_%d.bsc", zidx, xidx, yidx);
	FILE *fp = fopen(fpname, "w");
	if (!fp) {
		printf("Failed to open file to write data chunk.\n");
		return -1;
	}
	*/
	
	for (int j=0; j<ylen; ++j) {
		for (int i=0; i<xlen; ++i) {
			fprintf(fp, "%d %d ", xstart+i, ystart+j);
			for (int num=0; num<names->size; ++num) {
				fprintf(fp, "%d ", *(chunks+ylen*xlen*num+xlen*j+i));
			}
			fprintf(fp, "\n");
		}
	}
	// fclose(fp);
}

int split_dim2_int16(FILE *fp, int sd_id, varnames *names_dim2,
			int yidx, int xidx, int *start, int *stride, int *edge) {
	
	int idx = 0;
	int16 chunks[names_dim2->size][edge[0]][edge[1]];
	for (int idx=0; idx<names_dim2->size; ++idx) {
		readdataset(sd_id, names_dim2->names[idx], 
					start, stride, edge, chunks[idx]);
	}
	
	int ret = save2file_dim2_int16(fp, names_dim2, &(chunks[0][0][0]), yidx, xidx,
								start[0], start[1], edge[0], edge[1]);
	if (ret == FAIL) {
		printf("Failed to store data chunk into file.\n");
		return FAIL;
	}
	return SUCCEED;
}

int split_dim3_int16(FILE *fp, int sd_id, varnames *names_dim3,
					int zidx, int yidx, int xidx, int *start, 
					int *stride, int *edge) {
	
	int idx = 0;
	int16 chunks[names_dim3->size][edge[1]][edge[2]];
	for (int idx=0; idx<names_dim3->size; ++idx) {
		readdataset(sd_id, names_dim3->names[idx], 
					start, stride, edge, chunks[idx]);
	}
	
	int ret = save2file_dim3_int16(fp, names_dim3, &(chunks[0][0][0]),
								zidx, yidx, xidx, start[1], 
								start[2], edge[1], edge[2]);
	if (ret == FAIL) {
		printf("Failed to store data chunk into file.\n");
		return FAIL;
	}
	return SUCCEED;
}

int export2file_dim2_int16(char *dsname, int ystart, int xstart,
						int ylen, int xlen, int16 *dspiece) {
	// generate file name
	char fpname[FILENAME_LEN];
	memset (fpname, 0, FILENAME_LEN);
	sprintf(fpname, "%s.bsc", dsname);
	
	FILE *fp = fopen(fpname, "w");
	if (!fp) {
		printf("Failed to open file to write data chunk.\n");
		return -1;
	}
	for (int j=0; j<ylen; j++) {
		for (int i=0; i<xlen; i++) {
			fprintf(fp, "%d ", *(dspiece+xlen*j+i));
		}
		fprintf(fp, "\n");
	}
	fclose(fp);
}

void build_chunks_relation(int xnum, int ynum) {
	FILE *pfile = fopen("partition.info", "w");
	if (pfile == NULL) {
		printf("Open file to write partition information failed.\n");
		return;
	}
	
	for (int i=0; i<xnum; ++i) {
		for (int j=0; j<ynum; ++j) {
			char line[1024];
			memset(line, 0, 1024);
			
			int idx = j*xnum+i;
			// neighbors of left, top, right, bottom
			int neighbors[4] = {-1,-1,-1,-1};
			// left
			if (i-1 >= 0) {
				neighbors[0] = idx-1;
			}
			if (j-1 >= 0) {
				neighbors[1] = (j-1)*xnum+i;
			}
			if (i+1 < xnum) {
				neighbors[2] = idx+1;
			}
			if (j+1 < ynum) {
				neighbors[3] = (j+1)*xnum+i;
			}
			
			sprintf(line+strlen(line), "%d:", idx);
			for (int k=0; k<4; k++) {
				if (neighbors[k] != -1) {
					sprintf(line+strlen(line), "%d,", neighbors[k]);
				}
			}
			line[strlen(line)-1] = '\0';
			fprintf(pfile, "%s\n", line);
		}
	}
}

void generate_split_name(char *name, int x_idx, int y_idx) {
	sprintf(name, "split_%d_%d.bsc", x_idx, y_idx);
}

int main(int argc, char* argv[])
{
	int ret = -1;
	int i, j, k;
	
	char *hdf_filename = argv[1];
	int32 sd_id = SDstart(hdf_filename, DFACC_READ);
	if (sd_id == FAIL) {
		printf("Failed to open file %s.\n", hdf_filename);
		return FAIL;
	}
	
	// Retrieve the dimension attributes of the SD interface identifier
	int32 nx_idx = SDfindattr(sd_id, "nx");
	int32 ny_idx = SDfindattr(sd_id, "ny");
	int32 nz_idx = SDfindattr(sd_id, "nz");
	if (nx_idx == FAIL || ny_idx == FAIL || nz_idx == FAIL) {
		printf("Failed to retrieve dimension attributes.\n");
		return FAIL;
	}
	int32 nx = 0, ny = 0, nz = 0;
	SDreadattr(sd_id, nx_idx, &nx);
	SDreadattr(sd_id, ny_idx, &ny);
	SDreadattr(sd_id, nz_idx, &nz);
	
	
	// number of chunks along x-axis and y-axis
	int xnum = atoi(argv[2]);
	int ynum = atoi(argv[3]);
	
	build_chunks_relation(xnum, ynum);
    
	int xinterval = nx/xnum;
	int yinterval = ny/ynum;
	
	for (j=0; j<ynum; ++j) {
		for (i=0; i<xnum; ++i) {
			
			printf("begin to generate split %d.\n", j*xnum+i);
			
			char split_name[FILENAME_LEN];
			memset(split_name, 0, FILENAME_LEN);
			generate_split_name(split_name, i, j);
			FILE *fp = fopen(split_name, "a");
			if (!fp) {
				printf("Failed to open file to write data chunk.\n");
				return -1;
			}
			
			// writing 2d data into split file
			int32 start2d[2] = {j*yinterval, i*xinterval};
			int32 stride2d[2] = {1, 1};
			int32 edge2d[2] = {yinterval, xinterval};
			if ( (j+2)*yinterval > ny ) {
				edge2d[0] = ny - j*yinterval;
			}
			if ( (i+2)*xinterval > nx ) {
				edge2d[1] = nx - i*xinterval;
			}
			
			int number_of_var2d = 15;
			char* namearray2d[] = {"raing", "rainc", "prcrate1",
								"prcrate2", "prcrate3", "prcrate4",
								"snowdpth", "radsw", "rnflx",
								"radswnet", "radlwin",  "usflx",
								"vsflx", "ptsflx", "qvsflx"};
			// int16 chunks2d[number_of_var2d][edge2d[0]][edge2d[1]];
			// int16 chunks2d[edge2d[0]][edge2d[1]];
			int16 **chunks2d = new int16*[number_of_var2d];
			for (int idx=0; idx<number_of_var2d; ++idx) {
				chunks2d[idx] = new int16[edge2d[1]*edge2d[0]];
				readdataset(sd_id, namearray2d[idx], start2d, stride2d, edge2d, chunks2d[idx]);
				printf("read out 2D variable %s successfully.\n", namearray2d[idx]);
			}
			
			/* 
			  padding 3d data at the end of 2d data
			  Format:
			  '2d data' '3d data level 1' '3d data level 2' ... '3d data level nz'
			 */
			int number_of_var3d = 15;
			char* namearray3d[] = {"zp", "u", "v",
								"w", "pt", "p",
								"qv", "qc", "qr",
								"qi", "qs",  "qh",
								"tke", "kmh", "kmv"};
			int32 start3d[3] = {0, j*yinterval, i*xinterval};
			int32 stride3d[3] = {1, 1, 1};
			int32 edge3d[3] = {nz, yinterval, xinterval};
			if ( (j+2)*yinterval > ny ) {
				edge3d[1] = ny - j*yinterval;
			}
			if ( (i+2)*xinterval > nx ) {
				edge3d[2] = nx - i*xinterval;
			}								
			int16 **chunks3d = new int16*[number_of_var3d];
			for (int idx=0; idx<number_of_var3d; ++idx) {
				chunks3d[idx] = new int16[nz*edge3d[1]*edge3d[2]];
				readdataset(sd_id, namearray3d[idx], 
						start3d, stride3d, edge3d, chunks3d[idx]);
				printf("read out 3D variable %s successfully.\n", namearray3d[idx]);
			}
			
			printf("begin to write variables to file.\n");
			for (int pj=0; pj<yinterval; ++pj) {
				for (int pi=0; pi<xinterval; ++pi) {
					fprintf(fp, "%6d %6d ", start2d[1]+pi, start2d[0]+pj);
					int vk = 0;
					for (vk=0; vk<number_of_var2d; ++vk) {
						fprintf(fp, "%6d ", chunks2d[vk][xinterval*pj+pi]);
					}
					
					// for the purpose of reducing data size
					nz = 2;
					for (k=0; k<nz; ++k) {
						for (vk=0; vk<number_of_var3d; ++vk) {
							fprintf(fp, "%6d ", 
								chunks3d[vk][k*yinterval*xinterval+pj*xinterval+pi]);
						}
					}
					/*
					// expand data twice
					for (k=0; k<nz; ++k) {
						for (vk=0; vk<number_of_var3d; ++vk) {
							fprintf(fp, "%6d ", 
								chunks3d[vk][k*yinterval*xinterval+pj*xinterval+pi]);
						}
					}
					*/
					
					fprintf(fp, "\n");
				}
			}
			printf("write successfully.\n");
			
			/*
			for (k=0; k<nz; ++k) {
				start3d[0] = k;
				for (int idx=0; idx<number_of_var3d; ++idx) {
					readdataset(sd_id, namearray3d[idx], 
								start3d, stride3d, edge3d, chunks3d);
					fprintf(fp, "%d ", *(chunks3d
										+yinterval*xinterval*k
										+xinterval*j
										+i));
				}
			}
			delete [] chunks3d;
			*/
			for (int idx=0; idx<number_of_var2d; ++idx) {
				delete [] chunks2d[idx];
			}
			delete [] chunks2d;
			for (int idx=0; idx<number_of_var3d; ++idx) {
				delete [] chunks3d[idx];
			}
			delete [] chunks3d;
			fclose(fp);
			
			printf("generate split %d finished.\n", j*xnum+i);
		}
	}

	// Terminate access to the SD interface and close the file.
	ret = SDend(sd_id);
	if (ret == FAIL) {
		printf("Failed to close the HDF file.\n");
		return FAIL;
	}
}


